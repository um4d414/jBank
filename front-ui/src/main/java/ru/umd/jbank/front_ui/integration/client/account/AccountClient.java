package ru.umd.jbank.front_ui.integration.client.account;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.front_ui.integration.client.account.dto.*;

@FeignClient(
    name = "account-service",
    path = "/account"
)
public interface AccountClient {
    @GetMapping("/{id}")
    AccountDto getAccount(@PathVariable("id") Long id);

    @PostMapping
    AccountDto createAccount(@RequestBody CreateAccountRequestDto accountDto);

    @GetMapping("/by-username/{username}")
    AccountDto getAccountByUsername(@PathVariable("username") String username);

    @PostMapping("/{accountId}/bank-account")
    BankAccountDto createBankAccount(
        @PathVariable("accountId") Long accountId,
        @RequestBody CreateBankAccountRequestDto request
    );

    @GetMapping("/bank-account/{bankAccountId}/owner")
    Long getBankAccountOwnerId(@PathVariable("bankAccountId") Long bankAccountId);
}
