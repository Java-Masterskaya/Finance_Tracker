package ru.yandex.finance_tracker.dto.inner;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetMonthlyReportParam {
    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be greater than 2000")
    @Max(value = 2100, message = "Year must be less than 2100")
    private Integer year;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;
}
