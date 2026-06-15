package ru.yandex.finance_tracker.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryExpenseDto {
    private String category;
    private BigDecimal totalExpense;
}
