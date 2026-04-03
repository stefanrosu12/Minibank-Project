package minibank.service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import minibank.config.ExchangeRateProperties;
import minibank.dto.TransferRequest;
import minibank.exception.BusinessException;
import minibank.model.Account;
import minibank.model.Transaction;
import minibank.model.Transfer;
import minibank.repository.AccountRepository;
import minibank.repository.TransactionRepository;
import minibank.repository.TransferRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateProperties exchangeRates;

    private static final Set<String> SEPA_COUNTRIES = Set.of(
            "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IS", "IE", "IT",
            "LV", "LI", "LT", "LU", "MT", "MC", "NL", "NO", "PL", "PT", "RO", "SM", "SK", "SI", "ES", "SE", "CH", "GB", "AD", "VA"
    );

    private static final BigDecimal SAVINGS_DAILY_LIMIT_EUR = new BigDecimal("5000.00");

    @Transactional
    public Transfer createTransfer(TransferRequest request){

        //Idemp check(no duplicate tranfers)
        if(request.getIdempotencyKey() != null){
            Optional<Transfer> existingTransfer = transferRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if(existingTransfer.isPresent()){
                return existingTransfer.get();
            }
        }

        //Loading accounts
        Account source = accountRepository.findByIbanForUpdate(request.getSourceIban())
                .orElseThrow(() -> new BusinessException("Source account not found", HttpStatus.NOT_FOUND));
        Account target = accountRepository.findByIbanForUpdate(request.getTargetIban())
                .orElseThrow(() -> new BusinessException("Target account not found", HttpStatus.NOT_FOUND));

        //Valid SEPA rules
        if(!isSepa(source.getIban()) || !isSepa(target.getIban())){
            throw new BusinessException("Transfers are only allowed between SEPA member countries", HttpStatus.BAD_REQUEST);
        }

        //Conversion Math
        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal exchangeRate = null;
        BigDecimal convertedAmount = amount;

        if(!source.getCurrency().equals(target.getCurrency())){
            BigDecimal sourceToRon = exchangeRates.getRates().get(source.getCurrency());
            BigDecimal targetToRon = exchangeRates.getRates().get(target.getCurrency());

            exchangeRate = sourceToRon.divide(targetToRon, 6, RoundingMode.HALF_EVEN);
            convertedAmount = amount.multiply(exchangeRate).setScale(2,RoundingMode.HALF_EVEN);
        } else {
            exchangeRate = null;
            convertedAmount = null;
        }

        //Check Savings for daily limit
        if(source.getAccountType() == Account.AccountType.SAVINGS && source.getId() != 1L){
            checkSavingsDailyLimit(source, amount);
        }

        //Balance check
        if(source.getId() != 1L && source.getBalance().compareTo(amount) < 0){
            throw new BusinessException("Insufficient funds", HttpStatus.BAD_REQUEST);
        }

        //Balance update / Account save
        if(source.getId() != 1L){
            source.setBalance(source.getBalance().subtract(amount));
            accountRepository.save(source);
        }
        if(target.getId() != 1L){
            target.setBalance(target.getBalance().add(convertedAmount));
            accountRepository.save(target);
        }

        //target record create / save
        Transfer transfer = new Transfer();
        transfer.setSourceIban(source.getIban());
        transfer.setTargetIban(target.getIban());
        transfer.setAmount(amount);
        transfer.setCurrency(source.getCurrency());
        transfer.setTargetCurrency(target.getCurrency());
        transfer.setExchangeRate(exchangeRate);
        transfer.setConvertedAmount(source.getCurrency().equals(target.getCurrency()) ? null : convertedAmount);
        transfer.setIdempotencyKey(request.getIdempotencyKey());

        transfer = transferRepository.save(transfer);

        //transac ledger entries
        createLedgerEntries(source, target, transfer, amount, convertedAmount);

        return transfer;
    }

    //Helper methods
    private boolean isSepa(String iban){
        if(iban == null || iban.length() < 2) return false;
        return SEPA_COUNTRIES.contains(iban.substring(0,2).toUpperCase());
    }

    private void checkSavingsDailyLimit(Account source, BigDecimal amount){
        BigDecimal amountInEur = amount;

        if(!source.getCurrency().equals("EUR")){
            BigDecimal sourceToRon = exchangeRates.getRates().get(source.getCurrency());
            BigDecimal eurToRon = exchangeRates.getRates().get("EUR");
            BigDecimal rateToEur = sourceToRon.divide(eurToRon, 6, RoundingMode.HALF_EVEN);
            amountInEur = amount.multiply(rateToEur).setScale(2, RoundingMode.HALF_EVEN);
        }

        Instant now = Instant.now();
        Instant startOfDay = ZonedDateTime.ofInstant(now, ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

        List<Transaction> todaysOutGoings = transactionRepository.findOutgoingTransactionsForDay(source.getId(), startOfDay, endOfDay);

        BigDecimal dailyTotalInEur = BigDecimal.ZERO;
        for(Transaction t : todaysOutGoings){
            BigDecimal tAmountInEur = t.getAmount();
            if(!t.getCurrency().equals("EUR")){
                BigDecimal tSourceToRon = exchangeRates.getRates().get(t.getCurrency());
                BigDecimal eurToRon = exchangeRates.getRates().get("EUR");
                BigDecimal tRateToEur = tSourceToRon.divide(eurToRon, 6, RoundingMode.HALF_EVEN);
                tAmountInEur = t.getAmount().multiply(tRateToEur).setScale(2, RoundingMode.HALF_EVEN);
            }
            dailyTotalInEur = dailyTotalInEur.add(tAmountInEur);
        }

        if(dailyTotalInEur.add(amountInEur).compareTo(SAVINGS_DAILY_LIMIT_EUR) > 0){
            throw new BusinessException("Transfer exceeds daily savings limit of 5000", HttpStatus.BAD_REQUEST);
        }
    }
    
    private void createLedgerEntries(Account source, Account target, Transfer transfer, BigDecimal amount, BigDecimal convertedAmount){
        if(source.getId() != 1L){
            Transaction sourceTx = new Transaction();
            sourceTx.setAccount(source);
            sourceTx.setType(target.getId() == 1L ? Transaction.TransactionType.WITHDRAWAL : Transaction.TransactionType.TRANSFER_OUT);
            sourceTx.setAmount(amount);
            sourceTx.setCurrency(source.getCurrency());
            sourceTx.setBalanceAfter(source.getBalance());
            sourceTx.setCounterpartyIban(target.getId() == 1L ? null : target.getIban());
            sourceTx.setTransferId(transfer.getId());
            transactionRepository.save(sourceTx);
        }

        if(target.getId() != 1L){
            Transaction targetTx = new Transaction();
            targetTx.setAccount(target);
            targetTx.setType(source.getId() == 1L ? Transaction.TransactionType.DEPOSIT : Transaction.TransactionType.TRANSFER_IN);
            targetTx.setAmount(convertedAmount);
            targetTx.setCurrency(target.getCurrency());
            targetTx.setBalanceAfter(target.getBalance());
            targetTx.setCounterpartyIban(source.getId() == 1L ? null : source.getIban());
            targetTx.setTransferId(transfer.getId());
            transactionRepository.save(targetTx);
        }
    }

    public Transfer getTransfer(Long id){
        return transferRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Transfer not found", HttpStatus.NOT_FOUND));
    }

    public Page<Transfer> getTransfers(String iban, Instant from, Instant to, Pageable pageable){
        return transferRepository.findWithFilters(iban,from,to,pageable);
    }
}
