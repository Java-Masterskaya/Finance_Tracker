package ru.yandex.finance_tracker.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyReportDto {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balanceDifference;
    private List<CategoryExpenseDto> expenseByCategory;
}
