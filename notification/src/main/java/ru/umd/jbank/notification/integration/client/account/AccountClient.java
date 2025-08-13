package ru.umd.jbank.notification.integration.client.account;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.umd.jbank.notification.integration.client.account.dto.AccountDto;

@FeignClient(
    name = "gateway-service"
)
public interface AccountClient {
    @GetMapping("/account/{id}")
    AccountDto getAccount(@PathVariable("id") Long id);
}
