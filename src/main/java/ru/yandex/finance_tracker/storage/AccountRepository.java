package ru.yandex.finance_tracker.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.finance_tracker.model.Account;

public interface AccountRepository extends JpaRepository<Account, Integer> {
}