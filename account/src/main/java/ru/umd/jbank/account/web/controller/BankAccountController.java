package ru.umd.jbank.account.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.account.data.dto.BankAccountDto;
import ru.umd.jbank.account.service.AccountManager;

import java.math.BigDecimal;
import java.util.Currency;

@RestController
@RequiredArgsConstructor
public class BankAccountController {
    private final AccountManager accountManager;

    @PostMapping("/bank-account/{id}/deposit")
    void makeDeposit(@PathVariable Long id, @RequestParam("amount") BigDecimal amount) {
        accountManager.makeCashOperation(new AccountManager.CashOperation(
            id,
            AccountManager.CashOperation.Type.DEPOSIT,
            amount
        ));
    }

    @PostMapping("/bank-account/{id}/withdrawal")
    void makeWithdrawal(@PathVariable Long id, @RequestParam("amount") BigDecimal amount) {
        accountManager.makeCashOperation(new AccountManager.CashOperation(
            id,
            AccountManager.CashOperation.Type.WITHDRAWAL,
            amount
        ));
    }

    @PostMapping("/account/{accountId}/bank-account")
    public ResponseEntity<BankAccountDto> createBankAccount(
        @PathVariable Long accountId,
        @Valid @RequestBody CreateBankAccountRequest request
    ) {
        try {
            var createRequest = new AccountManager.CreateBankAccountRequestDto(
                accountId,
                request.currency()
            );

            var bankAccount = accountManager.createBankAccount(createRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(bankAccount);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public record CreateBankAccountRequest(
        @NotNull(message = "Валюта обязательна")
        Currency currency
    ) {
    }

}
