package io.github.alef2509.library.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListMemberLoans {

    List<LoanSummary> execute(UUID memberId);

    record LoanSummary(UUID loanId, UUID copyId, LocalDate borrowedOn, LocalDate dueOn, boolean overdue,
                       int renewals) {
    }
}
