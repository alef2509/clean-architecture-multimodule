package io.github.alef2509.library.application.usecase;

import io.github.alef2509.library.application.gateway.CopyGateway;
import io.github.alef2509.library.application.gateway.LoanGateway;
import io.github.alef2509.library.application.gateway.ReservationGateway;
import io.github.alef2509.library.application.gateway.UnitOfWork;
import io.github.alef2509.library.domain.Copy;
import io.github.alef2509.library.domain.Ids.LoanId;
import io.github.alef2509.library.domain.Loan;
import io.github.alef2509.library.domain.LoanPolicy;

import java.time.Clock;
import java.time.LocalDate;

public class RenewLoanInteractor implements RenewLoan {

    private final LoanGateway loans;
    private final CopyGateway copies;
    private final ReservationGateway reservations;
    private final UnitOfWork unitOfWork;
    private final LoanPolicy policy;
    private final Clock clock;

    public RenewLoanInteractor(LoanGateway loans, CopyGateway copies, ReservationGateway reservations,
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
            loan.renew(today, policy, !reservations.findWaiting(copy.isbn()).isEmpty());
            loans.save(loan);
            return new Response(loan.id().value(), loan.dueOn(), loan.renewals());
        });
    }
}
