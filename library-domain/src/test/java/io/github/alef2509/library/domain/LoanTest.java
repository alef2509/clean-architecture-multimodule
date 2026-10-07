package io.github.alef2509.library.domain;

import io.github.alef2509.library.domain.Ids.MemberId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanTest {

    private static final LoanPolicy POLICY = LoanPolicy.standard();
    private static final LocalDate DAY_1 = LocalDate.of(2026, 3, 1);

    private final MemberId ada = MemberId.next();
    private final Copy copy = Copy.newCopy(new Isbn("9780134685991"));

    @Test
    void lendingTakesTheCopyAndSetsTheDueDate() {
        Loan loan = Loan.start(copy, ada, DAY_1, POLICY);

        assertThat(loan.dueOn()).isEqualTo(DAY_1.plusDays(14));
        assertThat(copy.status()).isEqualTo(Copy.Status.ON_LOAN);
        assertThatThrownBy(() -> Loan.start(copy, MemberId.next(), DAY_1, POLICY))
                .hasMessageContaining("not available");
    }

    @Test
    void finesAreChargedPerDayLateAndCapped() {
        assertThat(Loan.start(Copy.newCopy(copy.isbn()), ada, DAY_1, POLICY).returnOn(DAY_1.plusDays(14), POLICY))
                .isEqualByComparingTo("0.00");
        assertThat(Loan.start(Copy.newCopy(copy.isbn()), ada, DAY_1, POLICY).returnOn(DAY_1.plusDays(20), POLICY))
                .isEqualByComparingTo("3.00");
        assertThat(Loan.start(Copy.newCopy(copy.isbn()), ada, DAY_1, POLICY).returnOn(DAY_1.plusDays(200), POLICY))
                .isEqualByComparingTo("20.00");
    }

    @Test
    void renewsOnceWhenNobodyIsWaiting() {
        Loan loan = Loan.start(copy, ada, DAY_1, POLICY);

        loan.renew(DAY_1.plusDays(10), POLICY, false);

        assertThat(loan.dueOn()).isEqualTo(DAY_1.plusDays(28));
        assertThatThrownBy(() -> loan.renew(DAY_1.plusDays(20), POLICY, false)).hasMessageContaining("renewed");
    }

    @Test
    void cannotRenewWhenOverdueOrReserved() {
        Loan overdue = Loan.start(copy, ada, DAY_1, POLICY);
        assertThatThrownBy(() -> overdue.renew(DAY_1.plusDays(15), POLICY, false))
                .isInstanceOfSatisfying(LibraryException.class, e -> assertThat(e.code()).isEqualTo("loan-overdue"));

        Loan reserved = Loan.start(Copy.newCopy(copy.isbn()), ada, DAY_1, POLICY);
        assertThatThrownBy(() -> reserved.renew(DAY_1.plusDays(5), POLICY, true))
                .isInstanceOfSatisfying(LibraryException.class, e -> assertThat(e.code()).isEqualTo("book-reserved"));
    }

    @Test
    void heldCopyIsReservedForOneMemberUntilTheHoldExpires() {
        MemberId bob = MemberId.next();
        copy.holdFor(bob, DAY_1.plusDays(3));

        assertThat(copy.canBeBorrowedBy(ada, DAY_1.plusDays(3))).isFalse();
        assertThat(copy.canBeBorrowedBy(bob, DAY_1.plusDays(3))).isTrue();
        assertThat(copy.canBeBorrowedBy(ada, DAY_1.plusDays(4))).isTrue(); // hold lapsed
    }
}
