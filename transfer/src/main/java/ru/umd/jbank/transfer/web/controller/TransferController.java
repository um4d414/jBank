package ru.umd.jbank.transfer.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.transfer.service.TransferService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
@Slf4j
public class TransferController {
    private final TransferService transferService;

    @PostMapping("/inner")
    public ResponseEntity<TransferResponse> innerTransfer(@Valid @RequestBody InnerTransferRequestDto request) {
        log.info("Получен запрос на внутренний перевод: {}", request);

        try {
            var transferRequest = new TransferService.InnerTransferRequest(
                request.accountId(),
                request.sourceBankAccountId(),
                request.targetBankAccountId(),
                request.amount()
            );

            transferService.innerTransfer(transferRequest);

            log.info("Внутренний перевод выполнен успешно для аккаунта {}", request.accountId());

            return ResponseEntity.ok(new TransferResponse(
                "SUCCESS",
                "Перевод выполнен успешно",
                request.amount()
            ));

        } catch (IllegalArgumentException e) {
            log.warn("Некорректные данные для перевода: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new TransferResponse(
                "VALIDATION_ERROR",
                e.getMessage(),
                null
            ));

        } catch (RuntimeException e) {
            log.error("Ошибка при выполнении перевода: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(new TransferResponse(
                "TRANSFER_ERROR",
                "Не удалось выполнить перевод: " + e.getMessage(),
                null
            ));
        }
    }

    @PostMapping("/external")
    public ResponseEntity<TransferResponse> externalTransfer(@Valid @RequestBody ExternalTransferRequestDto request) {
        log.info("Получен запрос на внешний перевод: {}", request);

        try {
            var transferRequest = new TransferService.ExternalTransferRequest(
                request.sourceAccountId(),
                request.sourceBankAccountId(),
                request.targetAccountId(),
                request.targetBankAccountId(),
                request.amount()
            );

            transferService.externalTransfer(transferRequest);

            log.info(
                "Внешний перевод выполнен успешно: с аккаунта {} на аккаунт {}",
                request.sourceAccountId(), request.targetAccountId()
            );

            return ResponseEntity.ok(new TransferResponse(
                "SUCCESS",
                "Внешний перевод выполнен успешно",
                request.amount()
            ));

        } catch (IllegalArgumentException e) {
            log.warn("Некорректные данные для внешнего перевода: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new TransferResponse(
                "VALIDATION_ERROR",
                e.getMessage(),
                null
            ));

        } catch (RuntimeException e) {
            log.error("Ошибка при выполнении внешнего перевода: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(new TransferResponse(
                "TRANSFER_ERROR",
                "Не удалось выполнить внешний перевод: " + e.getMessage(),
                null
            ));
        }
    }

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

    public record ExternalTransferRequestDto(
        @NotNull(message = "ID исходного аккаунта обязателен")
        Long sourceAccountId,

        @NotNull(message = "ID исходного банковского счета обязателен")
        Long sourceBankAccountId,

        @NotNull(message = "ID целевого аккаунта обязателен")
        Long targetAccountId,

        @NotNull(message = "ID целевого банковского счета обязателен")
        Long targetBankAccountId,

        @NotNull(message = "Сумма перевода обязательна")
        @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше 0")
        BigDecimal amount
    ) {
    }

    public record TransferResponse(
        String status,
        String message,
        BigDecimal processedAmount
    ) {
    }
}
