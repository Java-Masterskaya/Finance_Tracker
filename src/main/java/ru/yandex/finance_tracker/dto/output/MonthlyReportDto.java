package ru.yandex.finance_tracker.dto.output;

import lombok.Data;

import java.util.Map;

@Data
public class MonthlyReportDto {
    Float totalIncome;
    Float totalExpense;
    Map<String, Float> expenseByCategory;
}
