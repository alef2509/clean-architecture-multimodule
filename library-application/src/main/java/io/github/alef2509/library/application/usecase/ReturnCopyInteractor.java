package io.github.alef2509.library.application.usecase;

import io.github.alef2509.library.application.gateway.CopyGateway;
import io.github.alef2509.library.application.gateway.LoanGateway;
import io.github.alef2509.library.application.gateway.ReservationGateway;
import io.github.alef2509.library.application.gateway.UnitOfWork;
import io.github.alef2509.library.domain.Copy;
import io.github.alef2509.library.domain.Ids.LoanId;
import io.github.alef2509.library.domain.Loan;
import io.github.alef2509.library.domain.LoanPolicy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Closes the loan, computes the fine, and gives the copy to the first member
 * waiting for the book (on hold for a few days) or back to the shelf.
 */
public class ReturnCopyInteractor implements ReturnCopy {

    private final LoanGateway loans;
    private final CopyGateway copies;
    private final ReservationGateway reservations;
    private final UnitOfWork unitOfWork;
    private final LoanPolicy policy;
    private final Clock clock;

    public ReturnCopyInteractor(LoanGateway loans, CopyGateway copies, ReservationGateway reservations,
                                UnitOfWork unitOfWork, LoanPolicy policy, Clock clock) {
        this.loans = loans;
        this.copies = copies;
        this.reservations = reservations;
        this.unitOfWork = unitOfWork;
        this.policy = policy;
        this.clock = clock;
    }

    @Override
    public Response execute(Request request) {
        LocalDate today = LocalDate.now(clock);
        return unitOfWork.execute(() -> {
            Loan loan = loans.findById(new LoanId(request.loanId()))
                    .orElseThrow(() -> new NotFoundException("Loan", request.loanId()));
            Copy copy = copies.findById(loan.copyId())
                    .orElseThrow(() -> new NotFoundException("Copy", loan.copyId().value()));

            BigDecimal fine = loan.returnOn(today, policy);
            copy.returned();
            reservations.findWaiting(copy.isbn()).stream().findFirst()
                    .ifPresent(next -> copy.holdFor(next.memberId(), today.plus(policy.holdPeriod())));

            loans.save(loan);
            copies.save(copy);
            return new Response(loan.id().value(), fine, copy.status().name(),
                    copy.heldFor() == null ? null : copy.heldFor().value());
        });
    }
}
