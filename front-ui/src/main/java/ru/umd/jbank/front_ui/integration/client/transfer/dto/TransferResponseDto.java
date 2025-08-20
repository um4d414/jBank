package ru.umd.jbank.front_ui.integration.client.transfer.dto;

import java.math.BigDecimal;

public record TransferResponseDto(
    String status,
    String message,
    BigDecimal processedAmount
) {
}
