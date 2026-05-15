package ru.yandex.finance_tracker.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyReportDto {
    private Float totalIncome;
    private Float totalExpense;
    private List<CategoryExpenseDto> expenseByCategory;
}
