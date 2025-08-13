package ru.umd.jbank.account.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.umd.jbank.account.data.entity.BankAccountEntity;

import java.util.Currency;
import java.util.List;

public interface BankingAccountRepository extends JpaRepository<BankAccountEntity, Long> {
    List<BankAccountEntity> findAllByAccountId(Long id);

    boolean existsByAccountIdAndCurrency(Long accountId, Currency currency);
}
