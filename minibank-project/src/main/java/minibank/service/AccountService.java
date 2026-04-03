package minibank.service;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import minibank.dto.AccountCreateRequest;
import minibank.exception.BusinessException;
import minibank.model.Account;
import minibank.model.Transaction;
import minibank.repository.AccountRepository;
import minibank.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public Account createAccount(AccountCreateRequest request){
        if(!IBANValidator.getInstance().isValid(request.getIban())){
            throw new BusinessException("Invalid IBAN format", HttpStatus.BAD_REQUEST);
        }

        if(accountRepository.existsByIban(request.getIban())){
            throw new BusinessException("IBAN is already in use", HttpStatus.CONFLICT);
        }

        Account account = new Account();
        account.setOwnerName(request.getOwnerName());
        account.setIban(request.getIban());
        account.setCurrency(request.getCurrency());

        try{
            account.setAccountType(Account.AccountType.valueOf(request.getAccountType()));
        }catch (IllegalArgumentException e){
            throw new BusinessException("Invalid account type. Must be CHECKING or SAVINGS", HttpStatus.BAD_REQUEST);
        }

        return accountRepository.save(account);
    }

    public Account getAccount(Long id){
        return accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found", HttpStatus.NOT_FOUND));
    }

    public Page<Account> getAllAccounts(Pageable pageable){
        return accountRepository.findAll(pageable);
    }

    public Page<Transaction> getAccountTransactions(Long accountId, Pageable pageable){
        getAccount(accountId);
        return transactionRepository.findByAccountIdOrderByTimestampAsc(accountId, pageable);
    }
}
