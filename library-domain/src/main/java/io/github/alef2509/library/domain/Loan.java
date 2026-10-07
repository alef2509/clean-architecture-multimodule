package io.github.alef2509.library.domain;

import io.github.alef2509.library.domain.Ids.CopyId;
import io.github.alef2509.library.domain.Ids.LoanId;
import io.github.alef2509.library.domain.Ids.MemberId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Loan {

    private final LoanId id;
    private final CopyId copyId;
    private final MemberId memberId;
    private final LocalDate borrowedOn;
    private LocalDate dueOn;
    private int renewals;
    private LocalDate returnedOn;
    private BigDecimal fine;

    public Loan(LoanId id, CopyId copyId, MemberId memberId, LocalDate borrowedOn, LocalDate dueOn, int renewals,
                LocalDate returnedOn, BigDecimal fine) {
        this.id = id;
        this.copyId = copyId;
        this.memberId = memberId;
        this.borrowedOn = borrowedOn;
        this.dueOn = dueOn;
        this.renewals = renewals;
        this.returnedOn = returnedOn;
        this.fine = fine;
    }

    /** Lends the copy and starts the loan; the copy's availability is checked here. */
    public static Loan start(Copy copy, MemberId member, LocalDate today, LoanPolicy policy) {
        copy.lend(member, today);
        return new Loan(LoanId.next(), copy.id(), member, today, today.plus(policy.loanPeriod()), 0, null,
                BigDecimal.ZERO.setScale(2));
    }

    public boolean isActive() {
        return returnedOn == null;
    }

    public boolean isOverdue(LocalDate today) {
        return isActive() && today.isAfter(dueOn);
    }

    public void renew(LocalDate today, LoanPolicy policy, boolean someoneIsWaiting) {
        requireActive();
        if (isOverdue(today)) {
            throw new LibraryException("loan-overdue", "Overdue loans cannot be renewed");
        }
        if (renewals >= policy.maxRenewals()) {
            throw new LibraryException("renewal-limit", "Loan was already renewed %d time(s)".formatted(renewals));
        }
        if (someoneIsWaiting) {
            throw new LibraryException("book-reserved", "Another member is waiting for this book");
        }
        dueOn = dueOn.plus(policy.loanPeriod());
        renewals++;
    }

    /** @return the fine due: a fixed amount per day late, capped */
    public BigDecimal returnOn(LocalDate today, LoanPolicy policy) {
        requireActive();
        returnedOn = today;
        long daysLate = Math.max(0, ChronoUnit.DAYS.between(dueOn, today));
        fine = policy.finePerDay().multiply(BigDecimal.valueOf(daysLate)).min(policy.maxFine()).setScale(2);
        return fine;
    }

    private void requireActive() {
        if (!isActive()) {
            throw new LibraryException("loan-closed", "Loan %s was already returned".formatted(id.value()));
        }
    }

    public LoanId id() {
        return id;
    }

    public CopyId copyId() {
        return copyId;
    }

    public MemberId memberId() {
        return memberId;
    }

    public LocalDate borrowedOn() {
        return borrowedOn;
    }

    public LocalDate dueOn() {
        return dueOn;
    }

    public int renewals() {
        return renewals;
    }

    public LocalDate returnedOn() {
        return returnedOn;
    }

    public BigDecimal fine() {
        return fine;
    }
}
