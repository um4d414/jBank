package ru.umd.jbank.front_ui.integration.client.cash;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(
    name = "gateway-service",
    contextId = "cash-client",
    path = "/cash"
)
public interface CashClient {
    @PostMapping("/deposit/{bankAccountId}")
    void processDeposit(
        @PathVariable("bankAccountId") Long bankAccountId,
        @RequestParam("amount") BigDecimal amount
    );

    @PostMapping("/withdrawal/{bankAccountId}")
    void processWithdrawal(
        @PathVariable("bankAccountId") Long bankAccountId,
        @RequestParam("amount") BigDecimal amount
    );
}
