package ru.umd.jbank.notification.integration.client.account;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.umd.jbank.notification.integration.client.account.dto.AccountDto;

@FeignClient(
    name = "account-service",
    url = "${services.account.url}"
)
public interface AccountClient {
    @GetMapping("/account/{id}")
    AccountDto getAccount(@PathVariable("id") Long id);
}
