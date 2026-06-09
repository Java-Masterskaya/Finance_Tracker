package ru.yandex.finance_tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.finance_tracker.dto.output.CategoryExpenseDto;
import ru.yandex.finance_tracker.dto.output.MonthlyReportDto;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.storage.TransactionRepository;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl  implements ReportService{
    private final TransactionRepository transactionRepository;

    public MonthlyReportDto getMonthlyReport(Long userId, int year, int month) {
        log.info("Начало формирования отчета для пользователя ID: {} за период: {}-{}", userId, year, month);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
        log.debug("Рассчитаны границы периода: начало={}, конец={}", start, end);

        Float totalIncome = transactionRepository.sumAmountByUserIdAndDateBetween(userId, start, end, Type.INCOME);
        Float totalExpense = transactionRepository.sumAmountByUserIdAndDateBetween(userId, start, end, Type.EXPENSE);
        log.debug("Агрегированные данные: доход={}, расход={}", totalIncome, totalExpense);

        List<CategoryExpenseDto> byCategory = transactionRepository.getExpenseByCategory(userId, start, end);
        log.info("Найдено категорий расходов: {}", byCategory != null ? byCategory.size() : 0);

        return new MonthlyReportDto(
                totalIncome != null ? totalIncome : 0.0f,
                totalExpense != null ? totalExpense : 0.0f,
                byCategory
        );
    }
}