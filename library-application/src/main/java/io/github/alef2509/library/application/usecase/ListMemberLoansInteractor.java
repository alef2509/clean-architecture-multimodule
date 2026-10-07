package io.github.alef2509.library.application.usecase;

import io.github.alef2509.library.application.gateway.LoanGateway;
import io.github.alef2509.library.application.gateway.MemberGateway;
import io.github.alef2509.library.domain.Ids.MemberId;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ListMemberLoansInteractor implements ListMemberLoans {

    private final MemberGateway members;
    private final LoanGateway loans;
    private final Clock clock;

    public ListMemberLoansInteractor(MemberGateway members, LoanGateway loans, Clock clock) {
        this.members = members;
        this.loans = loans;
        this.clock = clock;
    }

    @Override
    public List<LoanSummary> execute(UUID memberId) {
        MemberId id = new MemberId(memberId);
        members.findById(id).orElseThrow(() -> new NotFoundException("Member", memberId));
        LocalDate today = LocalDate.now(clock);
        return loans.findActiveByMember(id).stream()
                .sorted(Comparator.comparing(l -> l.dueOn()))
                .map(l -> new LoanSummary(l.id().value(), l.copyId().value(), l.borrowedOn(), l.dueOn(),
                        l.isOverdue(today), l.renewals()))
                .toList();
    }
}
