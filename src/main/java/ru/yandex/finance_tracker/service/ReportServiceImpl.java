package ru.yandex.finance_tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.finance_tracker.dto.output.CategoryExpenseDto;
import ru.yandex.finance_tracker.dto.output.MonthlyReportDto;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.storage.TransactionRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public MonthlyReportDto getMonthlyReport(Long userId, int year, int month) {
        log.info("Начало формирования отчета для пользователя ID: {} за период: {}-{}", userId, year, month);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        ZoneId userZone = ZoneId.of(user.getTimezone());
        YearMonth yearMonth = YearMonth.of(year, month);

        ZonedDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay(userZone);
        ZonedDateTime startOfNextMonth = yearMonth.plusMonths(1).atDay(1).atStartOfDay(userZone);

        Instant startUTC = startOfMonth.toInstant();
        Instant endUTC = startOfNextMonth.toInstant();
        log.debug("Рассчитаны границы периода: начало={}, конец (не включая)={}", startUTC, endUTC);

        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndDateBetween(userId, startUTC, endUTC, Type.INCOME);
        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndDateBetween(userId, startUTC, endUTC, Type.EXPENSE);
        log.debug("Агрегированные данные: доход={}, расход={}", totalIncome, totalExpense);

        List<CategoryExpenseDto> byCategory = transactionRepository.getExpenseByCategory(userId, startUTC, endUTC);
        log.info("Найдено категорий расходов: {}", byCategory != null ? byCategory.size() : 0);
        BigDecimal defaultZero = new BigDecimal("0.00").setScale(2, RoundingMode.HALF_UP);

        BigDecimal incomeSafe = totalIncome != null ? totalIncome.setScale(2, RoundingMode.HALF_UP) : defaultZero;
        BigDecimal expenseSafe = totalExpense != null ? totalExpense.setScale(2, RoundingMode.HALF_UP) : defaultZero;

        BigDecimal balanceDifference = incomeSafe.subtract(expenseSafe);
        log.debug("Рассчитана чистая разница (баланс): {}", balanceDifference);

        return new MonthlyReportDto(
                incomeSafe,
                expenseSafe,
                balanceDifference,
                byCategory != null ? byCategory : List.of()
        );
    }
}
