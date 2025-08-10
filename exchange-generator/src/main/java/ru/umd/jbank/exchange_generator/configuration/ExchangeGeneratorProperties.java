package ru.umd.jbank.exchange_generator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "exchange.generator")
@Data
public class ExchangeGeneratorProperties {
    private List<String> currencies;
    private Map<String, BigDecimal> initialRates;
    private long intervalMs = 1000;
    private boolean enabled = true;
}
