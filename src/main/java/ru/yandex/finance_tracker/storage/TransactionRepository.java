package ru.yandex.finance_tracker.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.finance_tracker.dto.output.CategoryExpenseDto;
import ru.yandex.finance_tracker.model.Transaction;
import ru.yandex.finance_tracker.model.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdAndIsDeletedFalse(Long id);

    Page<Transaction> findByAccount_IdAndIsDeletedFalse(Long accountId, Pageable pageable);

    @Query("SELECT SUM(t.amount) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.date >= :start AND t.date < :end " +
            "AND t.type = :type " +
            "AND t.isDeleted = false")
    BigDecimal sumAmountByUserIdAndDateBetween(@Param("userId") Long userId,
                                               @Param("start") Instant start,
                                               @Param("end") Instant end,
                                               @Param("type") Type type);

    @Query("SELECT new ru.yandex.finance_tracker.dto.output.CategoryExpenseDto(t.category.name, SUM(t.amount)) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.date >= :start AND t.date < :end " +
            "AND t.type = 'EXPENSE' " +
            "AND t.isDeleted = false " +
            "GROUP BY t.category.id, t.category.name")
    List<CategoryExpenseDto> getExpenseByCategory(@Param("userId") Long userId,
                                                  @Param("start") Instant start,
                                                  @Param("end") Instant end);

    @Modifying
    @Query("UPDATE Transaction t SET t.isDeleted = true WHERE t.account.id = :accountId")
    void softDeleteAllByAccountId(@Param("accountId") Long accountId);
}
