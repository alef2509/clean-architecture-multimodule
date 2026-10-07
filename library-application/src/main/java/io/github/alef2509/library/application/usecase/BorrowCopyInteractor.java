package io.github.alef2509.library.application.usecase;

import io.github.alef2509.library.application.gateway.CopyGateway;
import io.github.alef2509.library.application.gateway.LoanGateway;
import io.github.alef2509.library.application.gateway.MemberGateway;
import io.github.alef2509.library.application.gateway.ReservationGateway;
import io.github.alef2509.library.application.gateway.UnitOfWork;
import io.github.alef2509.library.domain.Copy;
import io.github.alef2509.library.domain.Ids.CopyId;
import io.github.alef2509.library.domain.Ids.MemberId;
import io.github.alef2509.library.domain.LibraryException;
import io.github.alef2509.library.domain.Loan;
import io.github.alef2509.library.domain.LoanPolicy;
import io.github.alef2509.library.domain.Member;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Rules: the member exists, has no overdue loans and is below the loan limit of
 * their membership; the copy is available (or on hold for this member). If the
 * member was waiting for this book, the reservation is fulfilled.
 */
public class BorrowCopyInteractor implements BorrowCopy {

    private final MemberGateway members;
    private final CopyGateway copies;
    private final LoanGateway loans;
    private final ReservationGateway reservations;
    private final UnitOfWork unitOfWork;
    private final LoanPolicy policy;
    private final Clock clock;

    public BorrowCopyInteractor(MemberGateway members, CopyGateway copies, LoanGateway loans,
                                ReservationGateway reservations, UnitOfWork unitOfWork, LoanPolicy policy,
                                Clock clock) {
        this.members = members;
        this.copies = copies;
        this.loans = loans;
        this.reservations = reservations;
        this.unitOfWork = unitOfWork;
        this.policy = policy;
        this.clock = clock;
    }

    @Override
    public Response execute(Request request) {
        LocalDate today = LocalDate.now(clock);
        return unitOfWork.execute(() -> {
            Member member = members.findById(new MemberId(request.memberId()))
                    .orElseThrow(() -> new NotFoundException("Member", request.memberId()));
            Copy copy = copies.findById(new CopyId(request.copyId()))
                    .orElseThrow(() -> new NotFoundException("Copy", request.copyId()));

            List<Loan> active = loans.findActiveByMember(member.id());
            if (active.stream().anyMatch(loan -> loan.isOverdue(today))) {
                throw new LibraryException("has-overdue-loans", "Return overdue books before borrowing new ones");
            }
            if (active.size() >= member.membership().maxActiveLoans()) {
                throw new LibraryException("loan-limit", "%s members may have at most %d books"
                        .formatted(member.membership(), member.membership().maxActiveLoans()));
            }

            Loan loan = Loan.start(copy, member.id(), today, policy);
            copies.save(copy);
            loans.save(loan);
            reservations.findWaiting(copy.isbn()).stream()
                    .filter(r -> r.memberId().equals(member.id()))
                    .findFirst()
                    .ifPresent(r -> {
                        r.fulfilled();
                        reservations.save(r);
                    });
            return new Response(loan.id().value(), copy.id().value(), loan.dueOn());
        });
    }
}
