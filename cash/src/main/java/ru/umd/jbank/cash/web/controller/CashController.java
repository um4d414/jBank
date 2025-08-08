package ru.umd.jbank.cash.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.cash.service.CashService;
import ru.umd.jbank.cash.service.dto.CashOperationRequest;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class CashController {
    private final CashService cashService;

    @PostMapping("/cash/deposit/{bankAccountId}")
    public void processDeposit(@PathVariable Long bankAccountId, @RequestParam("amount") BigDecimal amount) {
        cashService.processDeposit(
            new CashOperationRequest(
                bankAccountId, amount, CashOperationRequest.Type.DEPOSIT
            )
        );
    }

    @PostMapping("/cash/withdrawal/{bankAccountId}")
    public void processWithdrawal(@PathVariable Long bankAccountId, @RequestParam("amount") BigDecimal amount) {
        cashService.processWithdrawal(
            new CashOperationRequest(
                bankAccountId, amount, CashOperationRequest.Type.WITHDRAWAL
            )
        );
    }
}
