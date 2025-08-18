package ru.umd.jbank.exchange_generator.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.umd.jbank.exchange_generator.configuration.ExchangeGeneratorProperties;
import ru.umd.jbank.exchange_generator.integration.client.ExchangeClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeGeneratorService {
    private final ExchangeGeneratorProperties properties;

    private final ExchangeClient exchangeClient;

    private final Map<String, BigDecimal> rates = new HashMap<>();

    @PostConstruct
    public void initialize() {
        generateRates();
        log.info("initial rates: {}", rates);
    }

    public void publishRates() {
        exchangeClient.saveRates(generateRates());
        log.info("published rates: {}", rates);
    }

    private Map<String, BigDecimal> generateRates() {
        properties
            .getCurrencies()
            .forEach(currency -> rates.put(currency, getNewRate(currency)));

        return rates;
    }

    private BigDecimal getNewRate(String currency) {
        if (rates.get(currency) == null) {
            rates.put(currency, properties.getInitialRates().get(currency));
        }

        if (currency.equals("RUB")) {
            return BigDecimal.ONE;
        }

        BigDecimal maxChange = rates.get(currency).multiply(new BigDecimal("0.005"));
        BigDecimal change = BigDecimal.valueOf((Math.random() - 0.5) * 2)
            .multiply(maxChange);

        return rates.get(currency)
            .add(change)
            .max(new BigDecimal("0.01"))
            .setScale(4, RoundingMode.HALF_EVEN);
    }
}
