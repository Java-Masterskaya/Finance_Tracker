package ru.yandex.finance_tracker.service;

import ru.yandex.finance_tracker.dto.output.MonthlyReportDto;

public interface ReportService {
    MonthlyReportDto getMonthlyReport(Long userId, int year, int month);
}
