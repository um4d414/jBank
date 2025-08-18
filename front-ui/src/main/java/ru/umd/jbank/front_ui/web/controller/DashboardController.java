
package ru.umd.jbank.front_ui.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.umd.jbank.front_ui.service.AccountService;
import ru.umd.jbank.front_ui.service.ExchangeService;
import ru.umd.jbank.front_ui.web.dto.ExchangeRateDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    private final AccountService accountService;

    private final ExchangeService exchangeService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("Загрузка главной страницы для пользователя: {}", username);

            // Получаем информацию о пользователе с его банковскими счетами
            var currentUser = accountService.getAccountByUsername(username);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userProfile", currentUser);

            // Банковские счета уже есть в AccountDto
            var bankAccounts = currentUser.bankingAccounts();
            model.addAttribute("bankAccounts", bankAccounts != null ? bankAccounts : Collections.emptyList());

            log.debug("Найдено {} банковских счетов для пользователя {}",
                      bankAccounts != null ? bankAccounts.size() : 0, username);

            // Получаем курсы валют
            List<ExchangeRateDto> exchangeRates = Collections.emptyList();
            try {
                log.debug("Начинаем загрузку курсов валют...");
                var rates = exchangeService.getAllRates();

                if (rates != null && !rates.isEmpty()) {
                    exchangeRates = rates.entrySet()
                        .stream()
                        .filter(entry -> !"RUB".equals(entry.getKey()))
                        .map(entry -> ExchangeRateDto.fromRate(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());

                    log.info("Загружено {} курсов валют", exchangeRates.size());
                    model.addAttribute("exchangeRates", exchangeRates);
                } else {
                    log.warn("Получен пустой список курсов валют");
                    model.addAttribute("exchangeRates", Collections.emptyList());
                    model.addAttribute("exchangeRatesError", "Курсы валют временно недоступны");
                }

            } catch (Exception e) {
                log.error("Ошибка при загрузке курсов валют", e);
                model.addAttribute("exchangeRates", Collections.emptyList());
                model.addAttribute("exchangeRatesError", "Ошибка загрузки курсов валют: " + e.getMessage());
            }

            return "dashboard";

        } catch (Exception e) {
            log.error("Ошибка при загрузке главной страницы", e);
            model.addAttribute("error", "Ошибка при загрузке данных: " + e.getMessage());
            model.addAttribute("exchangeRates", Collections.emptyList());
            return "dashboard";
        }
    }
}