package ru.umd.jbank.front_ui.integration.client.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InnerTransferRequestDto(
    @NotNull(message = "ID аккаунта обязателен")
    Long accountId,

    @NotNull(message = "ID исходного банковского счета обязателен")
    Long sourceBankAccountId,

    @NotNull(message = "ID целевого банковского счета обязателен")
    Long targetBankAccountId,

    @NotNull(message = "Сумма перевода обязательна")
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше 0")
    BigDecimal amount
) {
}
