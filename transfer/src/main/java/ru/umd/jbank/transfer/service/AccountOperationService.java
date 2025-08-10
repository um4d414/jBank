package ru.umd.jbank.transfer.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;
import ru.umd.jbank.transfer.integration.client.account.AccountClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountOperationService {
    private final AccountClient accountClient;

    @Retryable(
        retryFor = {FeignException.class, RuntimeException.class},
        noRetryFor = {IllegalArgumentException.class},
        maxAttempts = 3,
        backoff = @Backoff(
            delay = 1000,
            multiplier = 2,
            maxDelay = 5000
        )
    )
    public void makeWithdrawalWithRetry(Long bankAccountId, BigDecimal amount) {
        log.info("Попытка списания {} с счета {}", amount, bankAccountId);
        try {
            accountClient.makeWithdrawal(bankAccountId, amount);
            log.info("Списание выполнено успешно");
        } catch (Exception e) {
            log.warn("Ошибка при списании: {}, будет предпринята повторная попытка", e.getMessage());
            throw e;
        }
    }

    @Retryable(
        retryFor = {FeignException.class, RuntimeException.class},
        noRetryFor = {IllegalArgumentException.class},
        maxAttempts = 3,
        backoff = @Backoff(
            delay = 1000,
            multiplier = 2,
            maxDelay = 5000
        )
    )
    public void makeDepositWithRetry(Long bankAccountId, BigDecimal amount) {
        log.info("Попытка зачисления {} на счет {}", amount, bankAccountId);
        try {
            accountClient.makeDeposit(bankAccountId, amount);
            log.info("Зачисление выполнено успешно");
        } catch (Exception e) {
            log.warn("Ошибка при зачислении: {}, будет предпринята повторная попытка", e.getMessage());
            throw e;
        }
    }

    // Метод восстановления для списания
    @Recover
    public void recoverWithdrawal(Exception ex, Long bankAccountId, BigDecimal amount) {
        log.error(
            "Все попытки списания исчерпаны для счета {} на сумму {}. Ошибка: {}",
            bankAccountId, amount, ex.getMessage()
        );
        throw new RuntimeException("Не удалось выполнить списание после всех попыток", ex);
    }

    // Метод восстановления для зачисления
    @Recover
    public void recoverDeposit(Exception ex, Long bankAccountId, BigDecimal amount) {
        log.error(
            "Все попытки зачисления исчерпаны для счета {} на сумму {}. Ошибка: {}",
            bankAccountId, amount, ex.getMessage()
        );
        throw new RuntimeException("Не удалось выполнить зачисление после всех попыток", ex);
    }
}


