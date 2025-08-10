package ru.umd.jbank.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.umd.jbank.transfer.integration.client.account.AccountClient;
import ru.umd.jbank.transfer.integration.client.exchange.ExchangeClient;
import ru.umd.jbank.transfer.integration.client.notification.NotificationClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {
    private final AccountClient accountClient;

    private final ExchangeClient exchangeClient;

    private final AccountOperationService accountOperationService;

    private final NotificationClient notificationClient;

    public void innerTransfer(InnerTransferRequest innerTransferRequest) {
        log.info(
            "Начало внутреннего перевода: со счета {} на счет {} на сумму {}",
            innerTransferRequest.sourceBankAccountId,
            innerTransferRequest.targetBankAccountId,
            innerTransferRequest.amount
        );

        var account = accountClient.getAccount(innerTransferRequest.accountId);
        if (innerTransferRequest.sourceBankAccountId.equals(innerTransferRequest.targetBankAccountId)) {
            throw new IllegalArgumentException("Source and target bank accounts are the same");
        }

        var sourceBankAccount = account.bankingAccounts()
            .stream()
            .filter(it -> it.id().equals(innerTransferRequest.sourceBankAccountId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Source bank account not found"));

        var targetBankAccount = account.bankingAccounts()
            .stream()
            .filter(it -> it.id().equals(innerTransferRequest.targetBankAccountId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Target bank account not found"));

        BigDecimal amountToDeposit = innerTransferRequest.amount;

        if (!sourceBankAccount.currency().equals(targetBankAccount.currency())) {
            amountToDeposit = exchangeClient.calculate(
                sourceBankAccount.currency().getCurrencyCode(),
                targetBankAccount.currency().getCurrencyCode(),
                innerTransferRequest.amount
            );

            log.info(
                "Сумма после конвертации: {} {} -> {} {}",
                innerTransferRequest.amount,
                sourceBankAccount.currency().getCurrencyCode(),
                amountToDeposit,
                targetBankAccount.currency().getCurrencyCode()
            );
        }

        executeTransferWithRetryAndCompensation(
            innerTransferRequest.sourceBankAccountId,
            innerTransferRequest.targetBankAccountId,
            innerTransferRequest.amount,
            amountToDeposit
        );

        log.info(
            "Внутренний перевод успешно завершен: со счета {} на счет {} на сумму {}",
            innerTransferRequest.sourceBankAccountId,
            innerTransferRequest.targetBankAccountId,
            amountToDeposit
        );

        sendInnerTransferSuccessNotification(innerTransferRequest, amountToDeposit);
    }

    public void externalTransfer(ExternalTransferRequest externalTransferRequest) {
        log.info(
            "Начало внешнего перевода: со счета {} на аккаунт {} счет {} на сумму {}",
            externalTransferRequest.sourceBankAccountId,
            externalTransferRequest.targetAccountId,
            externalTransferRequest.targetBankAccountId,
            externalTransferRequest.amount
        );

        var sourceAccount = accountClient.getAccount(externalTransferRequest.sourceAccountId);
        var sourceBankAccount = sourceAccount
            .bankingAccounts()
            .stream()
            .filter(it -> it.id().equals(externalTransferRequest.sourceBankAccountId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Source bank account not found"));

        var targetAccount = accountClient.getAccount(externalTransferRequest.targetAccountId);
        var targetBankAccount = targetAccount
            .bankingAccounts()
            .stream()
            .filter(it -> it.id().equals(externalTransferRequest.targetBankAccountId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Target bank account not found"));

        if (externalTransferRequest.sourceAccountId.equals(externalTransferRequest.targetAccountId)) {
            throw new IllegalArgumentException("Source and target accounts are the same. Use innerTransfer instead");
        }

        BigDecimal amountToDeposit = externalTransferRequest.amount;

        if (!sourceBankAccount.currency().equals(targetBankAccount.currency())) {
            log.info(
                "Конвертация валюты: {} -> {}",
                sourceBankAccount.currency().getCurrencyCode(),
                targetBankAccount.currency().getCurrencyCode()
            );

            amountToDeposit = exchangeClient.calculate(
                sourceBankAccount.currency().getCurrencyCode(),
                targetBankAccount.currency().getCurrencyCode(),
                externalTransferRequest.amount
            );

            log.info(
                "Сумма после конвертации: {} {} -> {} {}",
                externalTransferRequest.amount,
                sourceBankAccount.currency().getCurrencyCode(),
                amountToDeposit,
                targetBankAccount.currency().getCurrencyCode()
            );
        }

        executeTransferWithRetryAndCompensation(
            externalTransferRequest.sourceBankAccountId,
            externalTransferRequest.targetBankAccountId,
            externalTransferRequest.amount,
            amountToDeposit
        );

        log.info(
            "Внешний перевод успешно завершен: со счета {} на счет {} на сумму {}",
            externalTransferRequest.sourceBankAccountId,
            externalTransferRequest.targetBankAccountId,
            amountToDeposit
        );

        sendExternalTransferSuccessNotifications(externalTransferRequest, amountToDeposit);
    }

    private void executeTransferWithRetryAndCompensation(
        Long sourceBankAccountId,
        Long targetBankAccountId,
        BigDecimal withdrawalAmount,
        BigDecimal depositAmount
    ) {
        boolean withdrawalCompleted = false;

        try {
            // Шаг 1: Списание с retry
            log.info("Выполнение списания {} с счета {}", withdrawalAmount, sourceBankAccountId);
            accountOperationService.makeWithdrawalWithRetry(sourceBankAccountId, withdrawalAmount);
            withdrawalCompleted = true;

            // Шаг 2: Зачисление с retry
            log.info("Выполнение зачисления {} на счет {}", depositAmount, targetBankAccountId);
            accountOperationService.makeDepositWithRetry(targetBankAccountId, depositAmount);

            log.info("Перевод успешно завершен");

        } catch (Exception e) {
            log.error("Ошибка при выполнении перевода: {}", e.getMessage());

            if (withdrawalCompleted) {
                try {
                    log.warn(
                        "Выполнение компенсирующей транзакции: возврат {} на счет {}",
                        withdrawalAmount, sourceBankAccountId
                    );
                    accountOperationService.makeDepositWithRetry(sourceBankAccountId, withdrawalAmount);
                    log.info("Компенсирующая транзакция выполнена успешно");
                } catch (Exception compensationError) {
                    log.error(
                        "КРИТИЧЕСКАЯ ОШИБКА: Не удалось выполнить компенсирующую транзакцию: {}",
                        compensationError.getMessage()
                    );
                    throw new RuntimeException("Критическая ошибка транзакции", compensationError);
                }
            }
            throw new RuntimeException("Перевод не выполнен", e);
        }
    }

    private void sendInnerTransferSuccessNotification(
        InnerTransferRequest request,
        BigDecimal finalAmount
    ) {
        try {
            String message = String.format(
                "✅ Внутренний перевод выполнен успешно!\n" +
                    "💸 Сумма: %s\n" +
                    "📤 Со счета: %s\n" +
                    "📥 На счет: %s\n" +
                    "🕒 Время: %s",
                finalAmount,
                request.sourceBankAccountId,
                request.targetBankAccountId,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            );

            notificationClient.notify(
                new NotificationClient.NotificationRequest(
                    request.accountId,
                    message
                )
            );

            log.info("Уведомление об успешном внутреннем переводе отправлено для аккаунта {}", request.accountId);
        } catch (Exception e) {
            log.error("Ошибка отправки уведомления о внутреннем переводе: {}", e.getMessage());
        }
    }

    private void sendExternalTransferSuccessNotifications(
        ExternalTransferRequest request,
        BigDecimal finalAmount
    ) {
        // Уведомление отправителю
        try {
            String senderMessage = String.format(
                "✅ Перевод отправлен успешно!\n" +
                    "💸 Сумма: %s\n" +
                    "📤 Со счета: %s\n" +
                    "👤 Получателю (аккаунт): %s\n" +
                    "📥 На счет: %s\n" +
                    "🕒 Время: %s",
                request.amount,
                request.sourceBankAccountId,
                request.targetAccountId,
                request.targetBankAccountId,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            );

            notificationClient.notify(
                new NotificationClient.NotificationRequest(
                    request.sourceAccountId,
                    senderMessage
                )
            );

            log.info(
                "Уведомление отправителю об успешном внешнем переводе отправлено для аккаунта {}",
                request.sourceAccountId
            );
        } catch (Exception e) {
            log.error("Ошибка отправки уведомления отправителю внешнего перевода: {}", e.getMessage());
        }

        try {
            String recipientMessage = String.format(
                "💰 Получен перевод!\n" +
                    "💸 Сумма: %s\n" +
                    "👤 От отправителя (аккаунт): %s\n" +
                    "📥 На ваш счет: %s\n" +
                    "🕒 Время: %s",
                finalAmount,
                request.sourceAccountId,
                request.targetBankAccountId,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            );

            notificationClient.notify(
                new NotificationClient.NotificationRequest(
                    request.targetAccountId,
                    recipientMessage
                )
            );

            log.info(
                "Уведомление получателю об успешном внешнем переводе отправлено для аккаунта {}",
                request.targetAccountId
            );
        } catch (Exception e) {
            log.error("Ошибка отправки уведомления получателю внешнего перевода: {}", e.getMessage());
        }
    }

    public record InnerTransferRequest(
        Long accountId,
        Long sourceBankAccountId,
        Long targetBankAccountId,
        BigDecimal amount
    ) {
    }

    public record ExternalTransferRequest(
        Long sourceAccountId,
        Long sourceBankAccountId,
        Long targetAccountId,
        Long targetBankAccountId,
        BigDecimal amount
    ) {
    }

}
