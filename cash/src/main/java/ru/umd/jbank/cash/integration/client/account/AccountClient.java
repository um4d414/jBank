package ru.umd.jbank.cash.integration.client.account;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.cash.integration.client.account.dto.AccountDto;

import java.math.BigDecimal;

@FeignClient(
    name = "account-service",
    url = "${services.account.url}"
)
public interface AccountClient {
    @GetMapping("/account/{id}")
    AccountDto getAccount(@PathVariable("id") Long id);

    @PostMapping("/bank-account/{id}/deposit")
    void makeDeposit(@PathVariable("id") Long id, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/bank-account/{id}/withdrawal")
    void makeWithdrawal(@PathVariable("id") Long id, @RequestParam("amount") BigDecimal amount);
}
