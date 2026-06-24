package ru.yandex.finance_tracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.finance_tracker.dto.output.MonthlyReportDto;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.storage.TransactionRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private static final Long USER_ID = 1L;
    private static final String DEFAULT_TIMEZONE = "Europe/Moscow";
    private static final Integer YEAR = 2026;
    private static final Integer MONTH = 6;
    private static final BigDecimal INCOME = new BigDecimal("1000.00");
    private static final BigDecimal EXPENSE = new BigDecimal("500.00");

    private User createUserWithTimezone(String timezone) {
        User user = new User();
        user.setId(USER_ID);
        user.setTimezone(timezone);
        return user;
    }

    @Test
    void shouldReturnMonthlyReportForUser() {
        User user = createUserWithTimezone(DEFAULT_TIMEZONE);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        when(transactionRepository.sumAmountByUserIdAndDateBetween(
                eq(USER_ID), any(Instant.class), any(Instant.class), eq(Type.INCOME)))
                .thenReturn(INCOME);
        when(transactionRepository.sumAmountByUserIdAndDateBetween(
                eq(USER_ID), any(Instant.class), any(Instant.class), eq(Type.EXPENSE)))
                .thenReturn(EXPENSE);
        when(transactionRepository.getExpenseByCategory(anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of());

        MonthlyReportDto report = reportService.getMonthlyReport(USER_ID, YEAR, MONTH);

        assertThat(report).isNotNull();
        assertThat(report.getTotalIncome()).isEqualByComparingTo(INCOME);
        assertThat(report.getTotalExpense()).isEqualByComparingTo(EXPENSE);
        assertThat(report.getExpenseByCategory()).isEmpty();
    }

    @Test
    void testThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getMonthlyReport(USER_ID, YEAR, MONTH))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with ID: " + USER_ID);
    }

    @Test
    void shouldHandleNullAggregations() {
        User user = createUserWithTimezone(DEFAULT_TIMEZONE);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        when(transactionRepository.sumAmountByUserIdAndDateBetween(
                anyLong(), any(Instant.class), any(Instant.class), any(Type.class)))
                .thenReturn(null);
        when(transactionRepository.getExpenseByCategory(anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(null);

        MonthlyReportDto report = reportService.getMonthlyReport(USER_ID, YEAR, MONTH);

        assertThat(report).isNotNull();
        assertThat(report.getTotalIncome()).isZero();
        assertThat(report.getTotalExpense()).isZero();
        assertThat(report.getExpenseByCategory()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "Europe/Moscow, 2025, 12, 100.0",
            "Europe/Moscow, 2026, 1, 0",
            "America/New_York, 2025, 12, 100.0",
            "America/New_York, 2026, 1, 0",
            "Asia/Tokyo, 2026, 1, 0"
    })
    void testMonthBoundaryWithDifferentTimezones(String timezone, int year, int month, BigDecimal expectedAmount) {
        User user = createUserWithTimezone(timezone);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        ArgumentCaptor<Instant> startCaptor1 = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCaptor1 = ArgumentCaptor.forClass(Instant.class);

        ArgumentCaptor<Instant> startCaptor2 = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCaptor2 = ArgumentCaptor.forClass(Instant.class);

        ArgumentCaptor<Instant> startCaptor3 = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCaptor3 = ArgumentCaptor.forClass(Instant.class);

        when(transactionRepository.sumAmountByUserIdAndDateBetween(
                eq(USER_ID), startCaptor1.capture(), endCaptor1.capture(), eq(Type.INCOME)))
                .thenReturn(expectedAmount);
        when(transactionRepository.sumAmountByUserIdAndDateBetween(
                eq(USER_ID), startCaptor2.capture(), endCaptor2.capture(), eq(Type.EXPENSE)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.getExpenseByCategory(eq(USER_ID), startCaptor3.capture(), endCaptor3.capture()))
                .thenReturn(List.of());

        MonthlyReportDto report = reportService.getMonthlyReport(USER_ID, year, month);

        assertThat(report.getTotalIncome()).isEqualByComparingTo(expectedAmount);

        ZonedDateTime expectedStart = YearMonth.of(year, month).atDay(1)
                .atStartOfDay(ZoneId.of(timezone));

        ZonedDateTime expectedEnd = YearMonth.of(year, month).plusMonths(1).atDay(1)
                .atStartOfDay(ZoneId.of(timezone));

        assertThat(startCaptor1.getValue()).isEqualTo(expectedStart.toInstant());
        assertThat(endCaptor1.getValue()).isEqualTo(expectedEnd.toInstant());
    }

    @Test
    void shouldCorrectlyDistributeAmountsOnMonthBoundary() {
        User user = createUserWithTimezone(DEFAULT_TIMEZONE);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCaptor = ArgumentCaptor.forClass(Instant.class);

        when(transactionRepository.sumAmountByUserIdAndDateBetween(
                eq(USER_ID), startCaptor.capture(), endCaptor.capture(), eq(Type.INCOME)))
                .thenReturn(INCOME);
        when(transactionRepository.sumAmountByUserIdAndDateBetween(
                eq(USER_ID), any(Instant.class), any(Instant.class), eq(Type.EXPENSE)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.getExpenseByCategory(anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of());

        reportService.getMonthlyReport(USER_ID, 2025, 12);

        ZonedDateTime expectedStart = YearMonth.of(2025, 12).atDay(1)
                .atStartOfDay(ZoneId.of(DEFAULT_TIMEZONE));
        ZonedDateTime expectedEnd = YearMonth.of(2025, 12).plusMonths(1).atDay(1)
                .atStartOfDay(ZoneId.of(DEFAULT_TIMEZONE));

        assertThat(startCaptor.getValue()).isEqualTo(expectedStart.toInstant());
        assertThat(endCaptor.getValue()).isEqualTo(expectedEnd.toInstant());
    }
}
