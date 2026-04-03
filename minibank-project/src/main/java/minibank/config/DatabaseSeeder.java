package minibank.config;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import minibank.model.Account;
import minibank.repository.AccountRepository;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {
    private final AccountRepository accountRepository;

    @Override
    public void run(String... args){
        String systemIban = "RO49AAAA1B31007593840000";

        if(!accountRepository.existsByIban(systemIban)){
            Account systemAccount = new Account();
            systemAccount.setOwnerName("System Bank");
            systemAccount.setIban(systemIban);
            systemAccount.setCurrency("RON");
            systemAccount.setAccountType(Account.AccountType.CHECKING);

            accountRepository.save(systemAccount);

            System.out.println("System Bank Account seeded successfully");
        }
    }
}
