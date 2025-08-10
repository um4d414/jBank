package ru.umd.jbank.exchange_generator.integration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(
    name = "exchange-service",
    url = "${services.exchange.url}"
)
public interface ExchangeClient {
    @PostMapping("/exchange/rates")
    void saveRates(@RequestBody Map<String, BigDecimal> rates);

    @GetMapping("/exchange/rates")
    Map<String, BigDecimal> getRates();

    @GetMapping("/exchange/rate/{currency}")
    BigDecimal getRate(@PathVariable("currency") String currency);

    @GetMapping("/exchange/calculate")
    BigDecimal calculate(
        @RequestParam("baseCurrency") String baseCurrency,
        @RequestParam("targetCurrency") String targetCurrency,
        @RequestParam("amount") BigDecimal amount
    );
}
