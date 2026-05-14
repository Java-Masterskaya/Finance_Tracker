package ru.yandex.finance_tracker.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.finance_tracker.model.Account;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findByUserId(Long userId);

    Optional<Account> findByIdAndUserId(Long id, Long userId);
}