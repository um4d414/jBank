package ru.umd.jbank.front_ui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.umd.jbank.front_ui.integration.client.cash.CashClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashService {
    private final CashClient cashClient;

    public void deposit(Long bankAccountId, BigDecimal amount) {
        try {
            log.info("Выполнение депозита: счет {}, сумма {}", bankAccountId, amount);

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Сумма должна быть положительной");
            }

            cashClient.processDeposit(bankAccountId, amount);
            log.info("Депозит успешно выполнен: счет {}, сумма {}", bankAccountId, amount);

        } catch (Exception e) {
            log.error("Ошибка при выполнении депозита: счет {}, сумма {}", bankAccountId, amount, e);
            handleCashOperationError(e, "депозита");
        }
    }

    public void withdrawal(Long bankAccountId, BigDecimal amount) {
        try {
            log.info("Выполнение снятия: счет {}, сумма {}", bankAccountId, amount);

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Сумма должна быть положительной");
            }

            cashClient.processWithdrawal(bankAccountId, amount);
            log.info("Снятие успешно выполнено: счет {}, сумма {}", bankAccountId, amount);

        } catch (Exception e) {
            log.error("Ошибка при выполнении снятия: счет {}, сумма {}", bankAccountId, amount, e);
            handleCashOperationError(e, "снятия");
        }
    }

    private void handleCashOperationError(Exception e, String operationType) {
        String message = e.getMessage();

        // Обработка специфичных ошибок от BlockerValidationFilter
        if (message != null && message.contains("OPERATION_BLOCKED")) {
            throw new CashOperationBlockedException("Операция " + operationType + " заблокирована системой безопасности");
        } else if (message != null && message.contains("VALIDATION_SERVICE_UNAVAILABLE")) {
            throw new CashOperationServiceUnavailableException("Сервис валидации временно недоступен. Попробуйте позже");
        } else if (message != null && message.contains("Insufficient")) {
            throw new InsufficientFundsException("Недостаточно средств на счете");
        } else {
            throw new CashOperationException("Ошибка при выполнении операции " + operationType + ": " + message);
        }
    }

    // Кастомные исключения
    public static class CashOperationException extends RuntimeException {
        public CashOperationException(String message) {
            super(message);
        }
    }

    public static class CashOperationBlockedException extends CashOperationException {
        public CashOperationBlockedException(String message) {
            super(message);
        }
    }

    public static class CashOperationServiceUnavailableException extends CashOperationException {
        public CashOperationServiceUnavailableException(String message) {
            super(message);
        }
    }

    public static class InsufficientFundsException extends CashOperationException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}
