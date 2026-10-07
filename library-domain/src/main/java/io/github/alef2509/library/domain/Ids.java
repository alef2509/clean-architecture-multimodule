package io.github.alef2509.library.domain;

import java.util.UUID;

/** Typed identifiers: a MemberId can never be passed where a CopyId is expected. */
public final class Ids {

    private Ids() {
    }

    public record CopyId(UUID value) {
        public static CopyId next() {
            return new CopyId(UUID.randomUUID());
        }
    }

    public record MemberId(UUID value) {
        public static MemberId next() {
            return new MemberId(UUID.randomUUID());
        }
    }

    public record LoanId(UUID value) {
        public static LoanId next() {
            return new LoanId(UUID.randomUUID());
        }
    }

    public record ReservationId(UUID value) {
        public static ReservationId next() {
            return new ReservationId(UUID.randomUUID());
        }
    }
}
