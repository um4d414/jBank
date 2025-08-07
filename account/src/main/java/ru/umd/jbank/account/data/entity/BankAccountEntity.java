package ru.umd.jbank.account.data.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.umd.jbank.account.data.entity.converter.CurrencyConverter;

import java.math.BigDecimal;
import java.util.Currency;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "account", name = "bank_accounts")
public class BankAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, length = 3)
    @Convert(converter = CurrencyConverter.class)
    private Currency currency;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountEntity account;
}
