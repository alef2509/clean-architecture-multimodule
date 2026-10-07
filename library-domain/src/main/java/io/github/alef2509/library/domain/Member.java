package io.github.alef2509.library.domain;

import io.github.alef2509.library.domain.Ids.MemberId;

public record Member(MemberId id, String name, Membership membership) {

    public enum Membership {
        STANDARD(3), PREMIUM(10);

        private final int maxActiveLoans;

        Membership(int maxActiveLoans) {
            this.maxActiveLoans = maxActiveLoans;
        }

        public int maxActiveLoans() {
            return maxActiveLoans;
        }
    }

    public Member {
        if (name == null || name.isBlank()) {
            throw new LibraryException("invalid-member", "Name is required");
        }
    }
}
