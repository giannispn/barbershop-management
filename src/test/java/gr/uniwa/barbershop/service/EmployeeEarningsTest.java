package gr.uniwa.barbershop.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the pure earnings formula in {@link EmployeeService}.
 * Earnings = base salary + (completed-service total * commissionRate / 100).
 * No database required.
 */
class EmployeeEarningsTest {

    @Test
    void baseSalaryOnly_whenNoCommission() {
        // 800 base, 0% commission, 500 of completed services -> just the base
        BigDecimal result = EmployeeService.computeEarnings(
            new BigDecimal("800"), new BigDecimal("0"), new BigDecimal("500"));
        assertEquals(new BigDecimal("800.00"), result);
    }

    @Test
    void baseSalaryOnly_whenNoCompletedServices() {
        // 800 base, 10% commission, but 0 completed services -> just the base
        BigDecimal result = EmployeeService.computeEarnings(
            new BigDecimal("800"), new BigDecimal("10"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("800.00"), result);
    }

    @Test
    void addsCommissionCorrectly() {
        // 800 base + 10% of 200 = 800 + 20 = 820
        BigDecimal result = EmployeeService.computeEarnings(
            new BigDecimal("800"), new BigDecimal("10"), new BigDecimal("200"));
        assertEquals(new BigDecimal("820.00"), result);
    }

    @Test
    void roundsToTwoDecimals() {
        // 0 base + 15% of 33.33 = 4.9995 -> rounds to 5.00
        BigDecimal result = EmployeeService.computeEarnings(
            BigDecimal.ZERO, new BigDecimal("15"), new BigDecimal("33.33"));
        assertEquals(new BigDecimal("5.00"), result);
    }

    @Test
    void fullCommissionRate() {
        // 1000 base + 100% of 300 = 1300
        BigDecimal result = EmployeeService.computeEarnings(
            new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("300"));
        assertEquals(new BigDecimal("1300.00"), result);
    }
}
