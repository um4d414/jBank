package ru.umd.jbank.exchange.data.repository;

import org.springframework.data.repository.CrudRepository;
import ru.umd.jbank.exchange.data.model.ExchangeRate;

import java.util.Optional;

public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, String> {
    Optional<ExchangeRate> findByCurrency(String currency);
}
