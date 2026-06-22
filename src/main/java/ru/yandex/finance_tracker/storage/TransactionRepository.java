package ru.yandex.finance_tracker.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.finance_tracker.dto.output.CategoryExpenseDto;
import ru.yandex.finance_tracker.model.Transaction;
import ru.yandex.finance_tracker.model.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccountId(Long accountId, PageRequest pageRequest);

    @Query("SELECT SUM(t.amount) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.date >= :start AND t.date <= :end AND t.type = :type")
    BigDecimal sumAmountByUserIdAndDateBetween(@Param("userId") Long userId,
                                               @Param("start") Instant start,
                                               @Param("end") Instant end,
                                               @Param("type") Type type);

    @Query("SELECT new ru.yandex.finance_tracker.dto.output.CategoryExpenseDto(t.category, SUM(t.amount)) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.date >= :start AND t.date <= :end AND t.type = 'EXPENSE' " +
            "GROUP BY t.category")
    List<CategoryExpenseDto> getExpenseByCategory(@Param("userId") Long userId,
                                                  @Param("start") Instant start,
                                                  @Param("end") Instant end);
}
