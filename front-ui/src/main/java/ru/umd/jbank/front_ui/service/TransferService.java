package ru.umd.jbank.front_ui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.umd.jbank.front_ui.integration.client.transfer.TransferClient;
import ru.umd.jbank.front_ui.integration.client.transfer.dto.*;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferService {
    private final TransferClient transferClient;

    public TransferResult processInnerTransfer(
        Long accountId, Long sourceBankAccountId,
        Long targetBankAccountId, BigDecimal amount
    ) {
        try {
            log.info(
                "Обработка внутреннего перевода: аккаунт {}, с {} на {}, сумма {}",
                accountId, sourceBankAccountId, targetBankAccountId, amount
            );

            if (sourceBankAccountId.equals(targetBankAccountId)) {
                throw new TransferValidationException("Нельзя переводить на тот же счет");
            }

            var request = InnerTransferRequestDto.builder()
                .accountId(accountId)
                .sourceBankAccountId(sourceBankAccountId)
                .targetBankAccountId(targetBankAccountId)
                .amount(amount)
                .build();

            ResponseEntity<TransferResponseDto> response = transferClient.innerTransfer(request);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                var body = response.getBody();
                log.info("Внутренний перевод успешно выполнен: {}", body.message());
                return new TransferResult(true, body.message(), body.processedAmount());
            } else {
                log.error("Неожиданный ответ от transfer-service: {}", response.getStatusCode());
                return new TransferResult(false, "Ошибка при выполнении перевода", null);
            }

        } catch (Exception e) {
            log.error("Ошибка при выполнении внутреннего перевода", e);
            return handleTransferError(e, "внутреннего перевода");
        }
    }

    public TransferResult processExternalTransfer(
        Long sourceAccountId,
        Long sourceBankAccountId,
        Long targetAccountId,
        Long targetBankAccountId,
        BigDecimal amount
    ) {
        try {
            log.info(
                "Обработка внешнего перевода: с аккаунта {} на аккаунт {}, сумма {}",
                sourceAccountId, targetAccountId, amount
            );

            if (sourceAccountId.equals(targetAccountId)) {
                throw new TransferValidationException("Нельзя переводить самому себе. Используйте внутренний перевод");
            }

            var request = ExternalTransferRequestDto.builder()
                .sourceAccountId(sourceAccountId)
                .sourceBankAccountId(sourceBankAccountId)
                .targetAccountId(targetAccountId)
                .targetBankAccountId(targetBankAccountId)
                .amount(amount)
                .build();

            ResponseEntity<TransferResponseDto> response = transferClient.externalTransfer(request);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                var body = response.getBody();
                log.info("Внешний перевод успешно выполнен: {}", body.message());
                return new TransferResult(true, body.message(), body.processedAmount());
            } else {
                log.error("Неожиданный ответ от transfer-service: {}", response.getStatusCode());
                return new TransferResult(false, "Ошибка при выполнении перевода", null);
            }

        } catch (Exception e) {
            log.error("Ошибка при выполнении внешнего перевода", e);
            return handleTransferError(e, "внешнего перевода");
        }
    }

    private TransferResult handleTransferError(Exception e, String operationType) {
        String message = e.getMessage();

        if (message != null && message.contains("OPERATION_BLOCKED")) {
            return new TransferResult(false, "Перевод заблокирован системой безопасности", null);
        } else if (message != null && message.contains("VALIDATION_SERVICE_UNAVAILABLE")) {
            return new TransferResult(false, "Сервис валидации временно недоступен. Попробуйте позже", null);
        } else if (message != null && message.contains("Insufficient")) {
            return new TransferResult(false, "Недостаточно средств на счете", null);
        } else if (e instanceof TransferValidationException) {
            return new TransferResult(false, message, null);
        } else {
            return new TransferResult(false, "Ошибка при выполнении " + operationType + ": " + message, null);
        }
    }

    public record TransferResult(
        boolean success,
        String message,
        BigDecimal processedAmount
    ) {
    }

    public static class TransferValidationException extends RuntimeException {
        public TransferValidationException(String message) {
            super(message);
        }
    }
}
