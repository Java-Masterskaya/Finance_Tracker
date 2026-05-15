package ru.yandex.finance_tracker.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryExpenseDto {
    String category;
    Float totalExpense;
}
