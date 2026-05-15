package ru.yandex.finance_tracker.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyReportDto {
    Float totalIncome;
    Float totalExpense;
    List<CategoryExpenseDto> expenseByCategory;
}
