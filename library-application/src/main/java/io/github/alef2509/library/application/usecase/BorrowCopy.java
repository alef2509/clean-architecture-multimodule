package io.github.alef2509.library.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

public interface BorrowCopy {

    Response execute(Request request);

    record Request(UUID memberId, UUID copyId) {
    }

    record Response(UUID loanId, UUID copyId, LocalDate dueOn) {
    }
}
