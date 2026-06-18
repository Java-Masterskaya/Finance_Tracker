package ru.yandex.finance_tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.finance_tracker.dto.output.CategoryExpenseDto;
import ru.yandex.finance_tracker.dto.output.MonthlyReportDto;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.security.utils.SecurityUtils;
import ru.yandex.finance_tracker.storage.TransactionRepository;

import java.time.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {
    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;

    public MonthlyReportDto getMonthlyReport(Long userId, int year, int month) {
        log.info("Начало формирования отчета для пользователя ID: {} за период: {}-{}", userId, year, month);

        ZoneId userZone = ZoneId.of(securityUtils.getCurrentUser().getTimezone());

        YearMonth yearMonth = YearMonth.of(year, month);
        ZonedDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay(userZone);
        ZonedDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(userZone);

        Instant startUTC = startOfMonth.toInstant();
        Instant endUTC = endOfMonth.toInstant();
        log.debug("Рассчитаны границы периода: начало={}, конец={}", startUTC, endUTC);

        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndDateBetween(userId,startUTC, endUTC, Type.INCOME);
        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndDateBetween(userId, startUTC, endUTC, Type.EXPENSE);
        log.debug("Агрегированные данные: доход={}, расход={}", totalIncome, totalExpense);

        List<CategoryExpenseDto> byCategory = transactionRepository.getExpenseByCategory(userId, start, end);
        log.info("Найдено категорий расходов: {}", byCategory != null ? byCategory.size() : 0);
        BigDecimal defaultZero = new BigDecimal("0.00").setScale(2, RoundingMode.HALF_UP);

        return new MonthlyReportDto(
                totalIncome != null ? totalIncome.setScale(2, RoundingMode.HALF_UP) : defaultZero,
                totalExpense != null ? totalExpense.setScale(2, RoundingMode.HALF_UP) : defaultZero,
                byCategory
        );
    }
}