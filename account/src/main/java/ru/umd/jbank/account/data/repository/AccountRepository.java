package ru.umd.jbank.account.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.umd.jbank.account.data.entity.AccountEntity;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
}
