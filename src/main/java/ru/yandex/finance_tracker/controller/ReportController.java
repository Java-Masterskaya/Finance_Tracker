package ru.yandex.finance_tracker.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.finance_tracker.dto.output.MonthlyReportDto;
import ru.yandex.finance_tracker.security.dto.AuthInfo;
import ru.yandex.finance_tracker.service.ReportService;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/monthly")
    @ResponseStatus(HttpStatus.OK)
    public MonthlyReportDto getMonthlyReport(@RequestParam @Min(2000) @Max(2100) int year,
                                             @RequestParam @Min(1) @Max(12) int month,
                                             @AuthenticationPrincipal AuthInfo authInfo) {
        return reportService.getMonthlyReport(authInfo.getId(), year, month);
    }
}