package ru.yandex.finance_tracker.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.finance_tracker.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findByAccountId(Integer accountId, PageRequest pageRequest);
}
