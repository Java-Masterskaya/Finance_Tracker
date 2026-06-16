package ru.yandex.finance_tracker.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.finance_tracker.dto.output.CategoryExpenseDto;
import ru.yandex.finance_tracker.model.Transaction;
import ru.yandex.finance_tracker.model.Type;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccountId(Long accountId, PageRequest pageRequest);

    @Query("SELECT SUM(t.amount) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.date >= :start AND t.date <= :end AND t.type = :type")
    Float sumAmountByUserIdAndDateBetween(@Param("userId") Long userId,
                                          @Param("start") LocalDate start,
                                          @Param("end") LocalDate end,
                                          @Param("type") Type type);

    @Query("SELECT new ru.yandex.finance_tracker.dto.output.CategoryExpenseDto(t.category, CAST(SUM(t.amount) AS float)) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.date >= :start AND t.date <= :end AND t.type = 'EXPENSE' " +
            "GROUP BY t.category")
    List<CategoryExpenseDto> getExpenseByCategory(@Param("userId") Long userId,
                                                  @Param("start") LocalDate start,
                                                  @Param("end") LocalDate end);
}
