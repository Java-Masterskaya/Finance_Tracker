package ru.yandex.finance_tracker.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.finance_tracker.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
}
