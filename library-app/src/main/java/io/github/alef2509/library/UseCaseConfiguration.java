package io.github.alef2509.library;

import io.github.alef2509.library.application.gateway.BookGateway;
import io.github.alef2509.library.application.gateway.CopyGateway;
import io.github.alef2509.library.application.gateway.LoanGateway;
import io.github.alef2509.library.application.gateway.MemberGateway;
import io.github.alef2509.library.application.gateway.ReservationGateway;
import io.github.alef2509.library.application.gateway.UnitOfWork;
import io.github.alef2509.library.application.usecase.BorrowCopy;
import io.github.alef2509.library.application.usecase.BorrowCopyInteractor;
import io.github.alef2509.library.application.usecase.ListMemberLoans;
import io.github.alef2509.library.application.usecase.ListMemberLoansInteractor;
import io.github.alef2509.library.application.usecase.RegisterBook;
import io.github.alef2509.library.application.usecase.RegisterBookInteractor;
import io.github.alef2509.library.application.usecase.RegisterMember;
import io.github.alef2509.library.application.usecase.RegisterMemberInteractor;
import io.github.alef2509.library.application.usecase.RenewLoan;
import io.github.alef2509.library.application.usecase.RenewLoanInteractor;
import io.github.alef2509.library.application.usecase.ReserveBook;
import io.github.alef2509.library.application.usecase.ReserveBookInteractor;
import io.github.alef2509.library.application.usecase.ReturnCopy;
import io.github.alef2509.library.application.usecase.ReturnCopyInteractor;
import io.github.alef2509.library.domain.LoanPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * The composition root ("Main" in Clean Architecture): plain constructors, no
 * annotations in the inner layers. Swapping an adapter means changing a bean here.
 */
@Configuration(proxyBeanMethods = false)
class UseCaseConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    LoanPolicy loanPolicy() {
        return LoanPolicy.standard();
    }

    @Bean
    RegisterBook registerBook(BookGateway books, CopyGateway copies, UnitOfWork uow) {
        return new RegisterBookInteractor(books, copies, uow);
    }

    @Bean
    RegisterMember registerMember(MemberGateway members, UnitOfWork uow) {
        return new RegisterMemberInteractor(members, uow);
    }

    @Bean
    BorrowCopy borrowCopy(MemberGateway members, CopyGateway copies, LoanGateway loans,
                          ReservationGateway reservations, UnitOfWork uow, LoanPolicy policy, Clock clock) {
        return new BorrowCopyInteractor(members, copies, loans, reservations, uow, policy, clock);
    }

    @Bean
    ReturnCopy returnCopy(LoanGateway loans, CopyGateway copies, ReservationGateway reservations, UnitOfWork uow,
                          LoanPolicy policy, Clock clock) {
        return new ReturnCopyInteractor(loans, copies, reservations, uow, policy, clock);
    }

    @Bean
    RenewLoan renewLoan(LoanGateway loans, CopyGateway copies, ReservationGateway reservations, UnitOfWork uow,
                        LoanPolicy policy, Clock clock) {
        return new RenewLoanInteractor(loans, copies, reservations, uow, policy, clock);
    }

    @Bean
    ReserveBook reserveBook(MemberGateway members, BookGateway books, CopyGateway copies,
                            ReservationGateway reservations, UnitOfWork uow, Clock clock) {
        return new ReserveBookInteractor(members, books, copies, reservations, uow, clock);
    }

    @Bean
    ListMemberLoans listMemberLoans(MemberGateway members, LoanGateway loans, Clock clock) {
        return new ListMemberLoansInteractor(members, loans, clock);
    }
}
