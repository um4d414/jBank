package ru.umd.jbank.transfer.integration.client.exchange;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(
    name = "exchange-service"
)
public interface ExchangeClient {
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
