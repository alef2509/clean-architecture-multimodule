package io.github.alef2509.library.domain;

import java.math.BigDecimal;
import java.time.Period;

/** Library-wide lending rules, kept together so they can change in one place. */
public record LoanPolicy(Period loanPeriod, int maxRenewals, BigDecimal finePerDay, BigDecimal maxFine,
                         Period holdPeriod) {

    public static LoanPolicy standard() {
        return new LoanPolicy(Period.ofDays(14), 1, new BigDecimal("0.50"), new BigDecimal("20.00"), Period.ofDays(3));
    }
}
