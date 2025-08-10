package ru.umd.jbank.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.umd.jbank.exchange.data.model.ExchangeRate;
import ru.umd.jbank.exchange.data.repository.ExchangeRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {
    private final ExchangeRateRepository exchangeRateRepository;

    public void saveRate(String currency, BigDecimal rate) {
        var exchangeRate = ExchangeRate.of(currency, rate);
        ExchangeRate saved = exchangeRateRepository.save(exchangeRate);
        log.info("Сохранен курс: {} = {}", saved.getCurrency(), saved.getRate());
    }

    public BigDecimal getRate(String currency) {
        return exchangeRateRepository
            .findByCurrency(currency)
            .map(ExchangeRate::getRate)
            .orElseThrow();
    }

    public BigDecimal calculateTargetCurrencyAmount(String baseCurrency, String targetCurrency, BigDecimal amount) {
        var baseCurrencyRate = getRate(baseCurrency);
        var targetCurrencyRate = getRate(targetCurrency);

        return amount.multiply(baseCurrencyRate).divide(targetCurrencyRate, 2, RoundingMode.HALF_UP);
    }

    public Map<String, BigDecimal> getAllRates() {
        return StreamSupport
            .stream(exchangeRateRepository.findAll().spliterator(), false)
            .collect(Collectors.toMap(
                ExchangeRate::getCurrency,
                ExchangeRate::getRate
            ));
    }
}
