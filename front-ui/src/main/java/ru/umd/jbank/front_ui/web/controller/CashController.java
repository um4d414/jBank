package ru.umd.jbank.front_ui.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.umd.jbank.front_ui.service.AccountService;
import ru.umd.jbank.front_ui.service.CashService;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CashController {
    private final CashService cashService;

    private final AccountService accountService;

    @PostMapping("/cash/operation")
    public String processCashOperation(
        @RequestParam("accountId") Long bankAccountId,
        @RequestParam("amount") BigDecimal amount,
        @RequestParam("operation") String operation,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {

        try {
            String username = authentication.getName();
            log.info(
                "Пользователь {} выполняет операцию {} на сумму {} для счета {}",
                username, operation, amount, bankAccountId
            );

            // Проверяем, что счет принадлежит пользователю
            var currentUser = accountService.getAccountByUsername(username);
            boolean isUserAccount = currentUser.bankingAccounts().stream()
                .anyMatch(account -> account.id().equals(bankAccountId));

            if (!isUserAccount) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    "Нельзя выполнять операции с чужими счетами"
                );
                return "redirect:/dashboard";
            }

            // Выполняем операцию
            switch (operation.toLowerCase()) {
                case "deposit" -> {
                    cashService.deposit(bankAccountId, amount);
                    redirectAttributes.addFlashAttribute(
                        "success",
                        String.format("Депозит на сумму %.2f успешно выполнен", amount)
                    );
                }
                case "withdraw" -> {
                    cashService.withdrawal(bankAccountId, amount);
                    redirectAttributes.addFlashAttribute(
                        "success",
                        String.format("Снятие на сумму %.2f успешно выполнено", amount)
                    );
                }
                default -> {
                    redirectAttributes.addFlashAttribute(
                        "error",
                        "Неизвестная операция: " + operation
                    );
                }
            }

        } catch (CashService.CashOperationBlockedException e) {
            log.warn("Операция заблокирована для пользователя {}: {}", authentication.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());

        } catch (CashService.CashOperationServiceUnavailableException e) {
            log.warn("Сервис недоступен для пользователя {}: {}", authentication.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());

        } catch (CashService.InsufficientFundsException e) {
            log.warn("Недостаточно средств для пользователя {}: {}", authentication.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("Неверные параметры от пользователя {}: {}", authentication.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());

        } catch (Exception e) {
            log.error(
                "Неожиданная ошибка при выполнении cash операции для пользователя {}",
                authentication.getName(), e
            );
            redirectAttributes.addFlashAttribute(
                "error",
                "Произошла ошибка при выполнении операции. Попробуйте позже."
            );
        }

        return "redirect:/dashboard";
    }
}
