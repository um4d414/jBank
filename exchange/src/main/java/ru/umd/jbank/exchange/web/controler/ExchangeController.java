package ru.umd.jbank.exchange.web.controler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.exchange.service.ExchangeService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ExchangeController {
    private final ExchangeService exchangeService;

    @PostMapping("/exchange/rates")
    public void saveRates(@RequestBody Map<String, BigDecimal> rates) {
        log.info("Получен запрос на сохранение курсов: {}", rates);
        rates.forEach(exchangeService::saveRate);
        log.info("Обработан запрос на сохранение {} курсов валют", rates.size());
    }

    @GetMapping("/exchange/rates")
    public Map<String, BigDecimal> getRates() {
        return exchangeService.getAllRates();
    }

    @GetMapping("/exchange/rate/{currency}")
    public BigDecimal getRate(@PathVariable String currency) {
        return exchangeService.getRate(currency);
    }

    @GetMapping("/exchange/calculate")
    public BigDecimal calculate(
        @RequestParam String baseCurrency,
        @RequestParam String targetCurrency,
        @RequestParam BigDecimal amount
    ) {
        return exchangeService.calculateTargetCurrencyAmount(baseCurrency, targetCurrency, amount);
    }
}
