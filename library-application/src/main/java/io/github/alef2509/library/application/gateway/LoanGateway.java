package io.github.alef2509.library.application.gateway;

import io.github.alef2509.library.domain.Ids.LoanId;
import io.github.alef2509.library.domain.Ids.MemberId;
import io.github.alef2509.library.domain.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanGateway {

    void save(Loan loan);

    Optional<Loan> findById(LoanId id);

    List<Loan> findActiveByMember(MemberId member);
}
