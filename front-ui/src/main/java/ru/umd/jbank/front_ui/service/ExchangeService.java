package ru.umd.jbank.front_ui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.umd.jbank.front_ui.integration.client.exchange.ExchangeClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {
    private final ExchangeClient exchangeClient;

    public Map<String, BigDecimal> getAllRates() {
        try {
            log.debug("Вызов ExchangeClient.getRates()...");
            var rates = exchangeClient.getRates();
            log.info("Успешно получено {} курсов валют: {}",
                     rates != null ? rates.size() : 0, rates);
            return rates;
        } catch (Exception e) {
            log.error("Ошибка при получении курсов валют от сервиса exchange", e);
            throw e;
        }
    }

    public BigDecimal getRate(String currency) {
        try {
            log.debug("Получение курса для валюты: {}", currency);
            var rate = exchangeClient.getRate(currency);
            log.debug("Курс для {}: {}", currency, rate);
            return rate;
        } catch (Exception e) {
            log.error("Ошибка при получении курса для валюты: {}", currency, e);
            throw e;
        }
    }

    public BigDecimal calculateExchange(String baseCurrency, String targetCurrency, BigDecimal amount) {
        try {
            log.debug("Расчет обмена: {} {} -> {}", amount, baseCurrency, targetCurrency);
            var result = exchangeClient.calculate(baseCurrency, targetCurrency, amount);
            log.debug("Результат обмена: {} {} = {} {}",
                      amount, baseCurrency, result, targetCurrency);
            return result;
        } catch (Exception e) {
            log.error("Ошибка при расчете обмена валют: {} {} -> {}",
                      amount, baseCurrency, targetCurrency, e);
            throw e;
        }
    }
}
