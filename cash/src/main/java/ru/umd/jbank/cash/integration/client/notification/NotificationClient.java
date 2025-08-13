package ru.umd.jbank.cash.integration.client.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "gateway-service"
)
public interface NotificationClient {
    @PostMapping("/notify")
    void notify(@Valid @RequestBody NotificationRequest request);

    record NotificationRequest(
        @NotNull(message = "ID аккаунта обязателен")
        Long accountId,

        @NotBlank(message = "Сообщение не может быть пустым")
        String message
    ) {}
}
