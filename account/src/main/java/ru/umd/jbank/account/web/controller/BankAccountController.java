package ru.umd.jbank.account.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.account.service.AccountManager;

import java.math.BigDecimal;

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
}
