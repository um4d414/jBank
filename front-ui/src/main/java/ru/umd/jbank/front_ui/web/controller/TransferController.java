package ru.umd.jbank.front_ui.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.umd.jbank.front_ui.service.AccountService;
import ru.umd.jbank.front_ui.service.TransferService;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TransferController {
    private final TransferService transferService;

    private final AccountService accountService;

    @PostMapping("/transfer/internal")
    public String processInternalTransfer(
        @RequestParam("fromAccountId") Long fromAccountId,
        @RequestParam("toAccountId") Long toAccountId,
        @RequestParam("amount") BigDecimal amount,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {

        try {
            String username = authentication.getName();
            log.info(
                "Пользователь {} выполняет внутренний перевод: с {} на {}, сумма {}",
                username, fromAccountId, toAccountId, amount
            );

            // Проверяем, что оба счета принадлежат пользователю
            var currentUser = accountService.getAccountByUsername(username);
            var userBankAccounts = currentUser.bankingAccounts();

            boolean fromAccountBelongsToUser = userBankAccounts.stream()
                .anyMatch(account -> account.id().equals(fromAccountId));
            boolean toAccountBelongsToUser = userBankAccounts.stream()
                .anyMatch(account -> account.id().equals(toAccountId));

            if (!fromAccountBelongsToUser || !toAccountBelongsToUser) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    "Можно переводить только между своими счетами"
                );
                return "redirect:/dashboard";
            }

            // Выполняем перевод
            var result = transferService.processInnerTransfer(
                currentUser.id(), fromAccountId, toAccountId, amount);

            if (result.success()) {
                redirectAttributes.addFlashAttribute("success", result.message());
            } else {
                redirectAttributes.addFlashAttribute("error", result.message());
            }

        } catch (Exception e) {
            log.error(
                "Неожиданная ошибка при внутреннем переводе для пользователя {}",
                authentication.getName(), e
            );
            redirectAttributes.addFlashAttribute(
                "error",
                "Произошла ошибка при выполнении перевода. Попробуйте позже."
            );
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/transfer/external")
    public String processExternalTransfer(
        @RequestParam("senderAccountId") Long senderAccountId,
        @RequestParam("receiverAccountId") Long receiverAccountId,
        @RequestParam("amount") BigDecimal amount,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {

        try {
            String username = authentication.getName();
            log.info(
                "Пользователь {} выполняет внешний перевод: с {} на {}, сумма {}",
                username, senderAccountId, receiverAccountId, amount
            );

            // Проверяем, что отправляющий счет принадлежит пользователю
            var currentUser = accountService.getAccountByUsername(username);
            var userBankAccounts = currentUser.bankingAccounts();

            boolean senderAccountBelongsToUser = userBankAccounts.stream()
                .anyMatch(account -> account.id().equals(senderAccountId));

            if (!senderAccountBelongsToUser) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    "Нельзя переводить с чужого счета"
                );
                return "redirect:/dashboard";
            }

            Long receiverAccountOwnerId;
            try {
                receiverAccountOwnerId = accountService.getBankAccountOwnerId(receiverAccountId);
                log.info("Найден владелец счета {}: аккаунт {}", receiverAccountId, receiverAccountOwnerId);
            } catch (Exception e) {
                log.error("Ошибка при поиске владельца счета {}", receiverAccountId, e);
                redirectAttributes.addFlashAttribute(
                    "error",
                    "Получатель не найден или счет недоступен"
                );
                return "redirect:/dashboard";
            }

            // Проверяем, что это не перевод самому себе
            if (currentUser.id().equals(receiverAccountOwnerId)) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    "Нельзя переводить самому себе. Используйте внутренний перевод"
                );
                return "redirect:/dashboard";
            }

            // Выполняем перевод
            var result = transferService.processExternalTransfer(
                currentUser.id(), senderAccountId,
                receiverAccountOwnerId, receiverAccountId, amount
            );

            if (result.success()) {
                redirectAttributes.addFlashAttribute("success", result.message());
            } else {
                redirectAttributes.addFlashAttribute("error", result.message());
            }

        } catch (Exception e) {
            log.error(
                "Неожиданная ошибка при внешнем переводе для пользователя {}",
                authentication.getName(), e
            );
            redirectAttributes.addFlashAttribute(
                "error",
                "Произошла ошибка при выполнении перевода. Попробуйте позже."
            );
        }

        return "redirect:/dashboard";
    }
}
