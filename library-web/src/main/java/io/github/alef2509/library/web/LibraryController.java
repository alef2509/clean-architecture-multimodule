package io.github.alef2509.library.web;

import io.github.alef2509.library.application.usecase.BorrowCopy;
import io.github.alef2509.library.application.usecase.ListMemberLoans;
import io.github.alef2509.library.application.usecase.RegisterBook;
import io.github.alef2509.library.application.usecase.RegisterMember;
import io.github.alef2509.library.application.usecase.RenewLoan;
import io.github.alef2509.library.application.usecase.ReserveBook;
import io.github.alef2509.library.application.usecase.ReturnCopy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * A thin controller: validate the HTTP input, build the use case request,
 * return the use case response. No business rule lives here.
 */
@RestController
@RequestMapping("/api/v1")
class LibraryController {

    record BookRequest(@NotBlank String isbn, @NotBlank @Size(max = 255) String title,
                       @Size(max = 255) String author, @Min(1) @Max(100) int copies) {
    }

    record MemberRequest(@NotBlank @Size(max = 100) String name, @NotBlank String membership) {
    }

    record LoanRequest(@NotNull UUID memberId, @NotNull UUID copyId) {
    }

    record ReservationRequest(@NotNull UUID memberId, @NotBlank String isbn) {
    }

    private final RegisterBook registerBook;
    private final RegisterMember registerMember;
    private final BorrowCopy borrowCopy;
    private final ReturnCopy returnCopy;
    private final RenewLoan renewLoan;
    private final ReserveBook reserveBook;
    private final ListMemberLoans listMemberLoans;

    LibraryController(RegisterBook registerBook, RegisterMember registerMember, BorrowCopy borrowCopy,
                      ReturnCopy returnCopy, RenewLoan renewLoan, ReserveBook reserveBook,
                      ListMemberLoans listMemberLoans) {
        this.registerBook = registerBook;
        this.registerMember = registerMember;
        this.borrowCopy = borrowCopy;
        this.returnCopy = returnCopy;
        this.renewLoan = renewLoan;
        this.reserveBook = reserveBook;
        this.listMemberLoans = listMemberLoans;
    }

    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    RegisterBook.Response registerBook(@Valid @RequestBody BookRequest r) {
        return registerBook.execute(new RegisterBook.Request(r.isbn(), r.title(), r.author(), r.copies()));
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    RegisterMember.Response registerMember(@Valid @RequestBody MemberRequest r) {
        return registerMember.execute(new RegisterMember.Request(r.name(), r.membership()));
    }

    @GetMapping("/members/{memberId}/loans")
    List<ListMemberLoans.LoanSummary> loans(@PathVariable UUID memberId) {
        return listMemberLoans.execute(memberId);
    }

    @PostMapping("/loans")
    @ResponseStatus(HttpStatus.CREATED)
    BorrowCopy.Response borrow(@Valid @RequestBody LoanRequest r) {
        return borrowCopy.execute(new BorrowCopy.Request(r.memberId(), r.copyId()));
    }

    @PostMapping("/loans/{loanId}/return")
    ReturnCopy.Response giveBack(@PathVariable UUID loanId) {
        return returnCopy.execute(new ReturnCopy.Request(loanId));
    }

    @PostMapping("/loans/{loanId}/renew")
    RenewLoan.Response renew(@PathVariable UUID loanId) {
        return renewLoan.execute(new RenewLoan.Request(loanId));
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    ReserveBook.Response reserve(@Valid @RequestBody ReservationRequest r) {
        return reserveBook.execute(new ReserveBook.Request(r.memberId(), r.isbn()));
    }
}
