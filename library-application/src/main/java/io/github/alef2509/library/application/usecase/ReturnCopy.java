package io.github.alef2509.library.application.usecase;

import java.math.BigDecimal;
import java.util.UUID;

public interface ReturnCopy {

    Response execute(Request request);

    record Request(UUID loanId) {
    }

    /** @param heldFor member the copy is now on hold for, or null if back on the shelf */
    record Response(UUID loanId, BigDecimal fine, String copyStatus, UUID heldFor) {
    }
}
