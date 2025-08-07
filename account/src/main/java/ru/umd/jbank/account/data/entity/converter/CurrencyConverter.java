package ru.umd.jbank.account.data.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Currency;

@Converter(autoApply = true)
public class CurrencyConverter implements AttributeConverter<Currency, String> {

    @Override
    public String convertToDatabaseColumn(Currency currency) {
        return currency != null ? currency.getCurrencyCode() : null;
    }

    @Override
    public Currency convertToEntityAttribute(String currencyCode) {
        return currencyCode != null ? Currency.getInstance(currencyCode) : null;
    }
}

