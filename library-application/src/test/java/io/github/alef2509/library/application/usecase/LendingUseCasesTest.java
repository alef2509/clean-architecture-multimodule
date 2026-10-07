package io.github.alef2509.library.application.usecase;

import io.github.alef2509.library.application.fixtures.InMemoryLibrary;
import io.github.alef2509.library.application.fixtures.MutableClock;
import io.github.alef2509.library.domain.Isbn;
import io.github.alef2509.library.domain.LibraryException;
import io.github.alef2509.library.domain.LoanPolicy;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The use cases wired to in-memory gateways: no database, no Spring, milliseconds. */
class LendingUseCasesTest {

    private static final String CLEAN_CODE = "9780132350884";

    private final InMemoryLibrary library = new InMemoryLibrary();
    private final MutableClock clock = new MutableClock(Instant.parse("2026-03-01T10:00:00Z"));
    private final LoanPolicy policy = LoanPolicy.standard();

    private final RegisterBook registerBook = new RegisterBookInteractor(library, library, library);
    private final RegisterMember registerMember = new RegisterMemberInteractor(library, library);
    private final BorrowCopy borrow = new BorrowCopyInteractor(library, library, library, library, library, policy, clock);
    private final ReturnCopy giveBack = new ReturnCopyInteractor(library, library, library, library, policy, clock);
    private final RenewLoan renew = new RenewLoanInteractor(library, library, library, library, policy, clock);
    private final ReserveBook reserve = new ReserveBookInteractor(library, library, library, library, library, clock);
    private final ListMemberLoans listLoans = new ListMemberLoansInteractor(library, library, clock);

    @Test
    void borrowingSetsADueDateTwoWeeksAhead() {
        UUID ada = member("STANDARD");
        UUID copy = copies(1).getFirst();

        BorrowCopy.Response loan = borrow.execute(new BorrowCopy.Request(ada, copy));

        assertThat(loan.dueOn()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(listLoans.execute(ada)).singleElement().satisfies(l -> assertThat(l.overdue()).isFalse());
    }

    @Test
    void membershipLimitsActiveLoans() {
        UUID ada = member("STANDARD");
        List<UUID> copies = copies(4);
        copies.subList(0, 3).forEach(c -> borrow.execute(new BorrowCopy.Request(ada, c)));

        assertThatThrownBy(() -> borrow.execute(new BorrowCopy.Request(ada, copies.get(3))))
                .isInstanceOfSatisfying(LibraryException.class, e -> assertThat(e.code()).isEqualTo("loan-limit"));

        UUID premium = member("PREMIUM");
        assertThat(borrow.execute(new BorrowCopy.Request(premium, copies.get(3)))).isNotNull();
    }

    @Test
    void overdueLoansBlockNewBorrowing() {
        UUID ada = member("PREMIUM");
        List<UUID> copies = copies(2);
        borrow.execute(new BorrowCopy.Request(ada, copies.get(0)));
        clock.advanceDays(15);

        assertThatThrownBy(() -> borrow.execute(new BorrowCopy.Request(ada, copies.get(1))))
                .isInstanceOfSatisfying(LibraryException.class, e -> assertThat(e.code()).isEqualTo("has-overdue-loans"));
        assertThat(listLoans.execute(ada)).singleElement().satisfies(l -> assertThat(l.overdue()).isTrue());
    }

    @Test
    void lateReturnIsFined() {
        UUID ada = member("STANDARD");
        UUID loan = borrow.execute(new BorrowCopy.Request(ada, copies(1).getFirst())).loanId();
        clock.advanceDays(18); // 4 days late

        ReturnCopy.Response returned = giveBack.execute(new ReturnCopy.Request(loan));

        assertThat(returned.fine()).isEqualByComparingTo("2.00");
        assertThat(returned.copyStatus()).isEqualTo("AVAILABLE");
        assertThat(listLoans.execute(ada)).isEmpty();
    }

    @Test
    void reservationQueueGetsTheReturnedCopyOnHold() {
        UUID ada = member("STANDARD");
        UUID bob = member("STANDARD");
        UUID carol = member("STANDARD");
        UUID copy = copies(1).getFirst();
        UUID loan = borrow.execute(new BorrowCopy.Request(ada, copy)).loanId();

        assertThat(reserve.execute(new ReserveBook.Request(bob, CLEAN_CODE)).position()).isEqualTo(1);
        assertThat(reserve.execute(new ReserveBook.Request(carol, CLEAN_CODE)).position()).isEqualTo(2);
        assertThatThrownBy(() -> renew.execute(new RenewLoan.Request(loan)))
                .isInstanceOfSatisfying(LibraryException.class, e -> assertThat(e.code()).isEqualTo("book-reserved"));

        ReturnCopy.Response returned = giveBack.execute(new ReturnCopy.Request(loan));

        assertThat(returned.copyStatus()).isEqualTo("ON_HOLD");
        assertThat(returned.heldFor()).isEqualTo(bob);
        assertThatThrownBy(() -> borrow.execute(new BorrowCopy.Request(carol, copy)))
                .isInstanceOfSatisfying(LibraryException.class, e -> assertThat(e.code()).isEqualTo("copy-unavailable"));
        borrow.execute(new BorrowCopy.Request(bob, copy)); // fulfils Bob's reservation
        assertThat(library.findWaiting(new Isbn(CLEAN_CODE)))
                .singleElement().satisfies(r -> assertThat(r.memberId().value()).isEqualTo(carol));
    }

    @Test
    void cannotReserveWhileACopyIsOnTheShelf() {
        UUID ada = member("STANDARD");
        copies(1);

        assertThatThrownBy(() -> reserve.execute(new ReserveBook.Request(ada, CLEAN_CODE)))
                .isInstanceOfSatisfying(LibraryException.class, e -> assertThat(e.code()).isEqualTo("copy-available"));
    }

    @Test
    void renewalExtendsTheDueDateOnce() {
        UUID ada = member("STANDARD");
        UUID loan = borrow.execute(new BorrowCopy.Request(ada, copies(1).getFirst())).loanId();
        clock.advanceDays(10);

        RenewLoan.Response renewed = renew.execute(new RenewLoan.Request(loan));

        assertThat(renewed.dueOn()).isEqualTo(LocalDate.of(2026, 3, 29));
        assertThatThrownBy(() -> renew.execute(new RenewLoan.Request(loan)))
                .isInstanceOfSatisfying(LibraryException.class, e -> assertThat(e.code()).isEqualTo("renewal-limit"));
    }

    @Test
    void unknownIdsAreReportedAsNotFound() {
        assertThatThrownBy(() -> borrow.execute(new BorrowCopy.Request(UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(NotFoundException.class);
    }

    private UUID member(String membership) {
        return registerMember.execute(new RegisterMember.Request("Member " + UUID.randomUUID(), membership)).memberId();
    }

    private List<UUID> copies(int n) {
        return registerBook.execute(new RegisterBook.Request(CLEAN_CODE, "Clean Code", "Robert C. Martin", n)).copyIds();
    }
}
