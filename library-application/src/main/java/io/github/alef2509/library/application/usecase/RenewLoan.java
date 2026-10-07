package io.github.alef2509.library.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

public interface RenewLoan {

    Response execute(Request request);

    record Request(UUID loanId) {
    }

    record Response(UUID loanId, LocalDate dueOn, int renewals) {
    }
}
