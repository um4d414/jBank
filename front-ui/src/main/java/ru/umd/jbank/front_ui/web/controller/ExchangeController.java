package ru.umd.jbank.front_ui.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.front_ui.service.ExchangeService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ExchangeController {
    private final ExchangeService exchangeService;

    @GetMapping("/api/exchange/rates")
    @ResponseBody
    public ResponseEntity<?> getRates() {
        try {
            var rates = exchangeService.getAllRates()
                .entrySet()
                .stream()
                .filter(entry -> !"RUB".equals(entry.getKey()))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));

            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            log.error("Ошибка при получении курсов валют через API", e);
            return ResponseEntity.internalServerError().body("Ошибка получения курсов валют");
        }
    }

    @GetMapping("/api/exchange/rate/{currency}")
    @ResponseBody
    public ResponseEntity<?> getRate(@PathVariable String currency) {
        try {
            if ("RUB".equals(currency)) {
                return ResponseEntity.ok(BigDecimal.ONE);
            }

            var rate = exchangeService.getRate(currency);
            if (rate != null) {
                return ResponseEntity.ok(rate);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при получении курса для валюты: {}", currency, e);
            return ResponseEntity.internalServerError().body("Ошибка получения курса валюты");
        }
    }

    @GetMapping("/api/exchange/calculate")
    @ResponseBody
    public ResponseEntity<?> calculate(
        @RequestParam String baseCurrency,
        @RequestParam String targetCurrency,
        @RequestParam BigDecimal amount
    ) {
        try {
            BigDecimal result;

            if ("RUB".equals(baseCurrency) && "RUB".equals(targetCurrency)) {
                result = amount;
            } else if ("RUB".equals(targetCurrency)) {
                var rate = exchangeService.getRate(baseCurrency);
                if (rate == null) {
                    return ResponseEntity.badRequest().body("Курс для валюты " + baseCurrency + " не найден");
                }
                result = amount.multiply(rate);
            } else if ("RUB".equals(baseCurrency)) {
                var rate = exchangeService.getRate(targetCurrency);
                if (rate == null) {
                    return ResponseEntity.badRequest().body("Курс для валюты " + targetCurrency + " не найден");
                }
                result = amount.divide(rate, 4, java.math.RoundingMode.HALF_UP);
            } else {
                var baseRate = exchangeService.getRate(baseCurrency);
                var targetRate = exchangeService.getRate(targetCurrency);

                if (baseRate == null || targetRate == null) {
                    return ResponseEntity.badRequest().body("Не удается получить курсы валют");
                }

                BigDecimal rubAmount = amount.multiply(baseRate);
                result = rubAmount.divide(targetRate, 4, java.math.RoundingMode.HALF_UP);
            }

            return ResponseEntity.ok(Map.of(
                "baseCurrency", baseCurrency,
                "targetCurrency", targetCurrency,
                "amount", amount,
                "result", result
            ));

        } catch (Exception e) {
            log.error("Ошибка при расчете обмена валют", e);
            return ResponseEntity.internalServerError().body("Ошибка расчета обмена валют");
        }
    }
}
