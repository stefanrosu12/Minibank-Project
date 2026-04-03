package minibank.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import minibank.dto.AccountCreateRequest;
import minibank.model.Account;
import minibank.model.Transaction;
import minibank.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody AccountCreateRequest request){
        Account createdAccount = accountService.createAccount(request);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable Long accountId){
        Account account = accountService.getAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    public ResponseEntity<Page<Account>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accounts = accountService.getAllAccounts(pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<Page<Transaction>> getAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = accountService.getAccountTransactions(accountId, pageable);
        return ResponseEntity.ok(transactions);
    }
}
