package ru.yandex.finance_tracker.storage;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.finance_tracker.model.Account;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.user.id = :userId")
    Optional<Account> findByIdAndUserIdWithLock(@Param("id") Long id, @Param("userId") Long userId);

    List<Account> findByUserId(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}