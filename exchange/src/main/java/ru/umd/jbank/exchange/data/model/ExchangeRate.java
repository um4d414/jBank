package ru.umd.jbank.exchange.data.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "exchange_rate", timeToLive = 60)
public class ExchangeRate {
    @Id
    private String currency;

    private BigDecimal rate;

    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public static ExchangeRate of(String currency, BigDecimal rate) {
        return ExchangeRate
            .builder()
            .currency(currency)
            .rate(rate)
            .build();
    }

    public void updateRate(BigDecimal newRate) {
        this.rate = newRate;
        this.lastUpdated = LocalDateTime.now();
    }
}
