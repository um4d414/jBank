package ru.umd.jbank.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.umd.jbank.cash.integration.client.account.AccountClient;
import ru.umd.jbank.cash.integration.client.notification.NotificationClient;
import ru.umd.jbank.cash.service.dto.CashOperationRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashService {
    private final AccountClient accountClient;

    private final NotificationClient notificationClient;

    public void processDeposit(CashOperationRequest cashOperationRequest) {
        accountClient.makeDeposit(cashOperationRequest.accountId(), cashOperationRequest.amount());

        sendDepositSuccessNotification(cashOperationRequest);

        log.info("Пополнение счета {} успешно завершено", cashOperationRequest.accountId());
    }

    public void processWithdrawal(CashOperationRequest cashOperationRequest) {
        accountClient.makeWithdrawal(cashOperationRequest.accountId(), cashOperationRequest.amount());

        sendWithdrawalSuccessNotification(cashOperationRequest);

        log.info("Снятие со счета {} успешно завершено", cashOperationRequest.accountId());
    }

    private void sendDepositSuccessNotification(CashOperationRequest request) {
        try {
            String message = String.format(
                "💰 Пополнение счета выполнено успешно!\n" +
                    "💸 Сумма пополнения: %s\n" +
                    "📥 Счет: %s\n" +
                    "💳 Средства зачислены на баланс\n" +
                    "🕒 Время операции: %s\n" +
                    "✅ Статус: Выполнено",
                request.amount(),
                request.accountId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
            );

            notificationClient.notify(
                new NotificationClient.NotificationRequest(
                    request.accountId(),
                    message
                )
            );

            log.info("Уведомление о пополнении отправлено для счета {}", request.accountId());
        } catch (Exception e) {
            log.error("Ошибка отправки уведомления о пополнении для счета {}: {}",
                      request.accountId(), e.getMessage());
        }
    }

    private void sendWithdrawalSuccessNotification(CashOperationRequest request) {
        try {
            String message = String.format(
                "💳 Снятие средств выполнено успешно!\n" +
                    "💸 Сумма снятия: %s\n" +
                    "📤 Счет: %s\n" +
                    "💰 Средства списаны с баланса\n" +
                    "🕒 Время операции: %s\n" +
                    "✅ Статус: Выполнено\n" +
                    "🏧 Получите наличные в банкомате",
                request.amount(),
                request.accountId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
            );

            notificationClient.notify(
                new NotificationClient.NotificationRequest(
                    request.accountId(),
                    message
                )
            );

            log.info("Уведомление о снятии отправлено для счета {}", request.accountId());
        } catch (Exception e) {
            log.error("Ошибка отправки уведомления о снятии для счета {}: {}",
                      request.accountId(), e.getMessage());
        }
    }

}
