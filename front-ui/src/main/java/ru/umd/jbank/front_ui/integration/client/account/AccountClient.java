package ru.umd.jbank.front_ui.integration.client.account;

import ru.umd.jbank.front_ui.integration.client.account.dto.AccountDto;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "gateway-service"
)
public interface AccountClient {
    @GetMapping("/account/{id}")
    AccountDto getAccount(@PathVariable("id") Long id);

}
