package io.github.alef2509.library.application.fixtures;

import io.github.alef2509.library.application.gateway.BookGateway;
import io.github.alef2509.library.application.gateway.CopyGateway;
import io.github.alef2509.library.application.gateway.LoanGateway;
import io.github.alef2509.library.application.gateway.MemberGateway;
import io.github.alef2509.library.application.gateway.ReservationGateway;
import io.github.alef2509.library.application.gateway.UnitOfWork;
import io.github.alef2509.library.domain.Book;
import io.github.alef2509.library.domain.Copy;
import io.github.alef2509.library.domain.Ids.CopyId;
import io.github.alef2509.library.domain.Ids.LoanId;
import io.github.alef2509.library.domain.Ids.MemberId;
import io.github.alef2509.library.domain.Isbn;
import io.github.alef2509.library.domain.Loan;
import io.github.alef2509.library.domain.Member;
import io.github.alef2509.library.domain.Reservation;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Every gateway in memory: use cases are tested in milliseconds, with no
 * database and no Spring. Shared as test fixtures with the outer modules.
 */
public class InMemoryLibrary implements BookGateway, CopyGateway, MemberGateway, LoanGateway, ReservationGateway,
        UnitOfWork {

    private final Map<Isbn, Book> books = new LinkedHashMap<>();
    private final Map<CopyId, Copy> copies = new LinkedHashMap<>();
    private final Map<MemberId, Member> members = new LinkedHashMap<>();
    private final Map<LoanId, Loan> loans = new LinkedHashMap<>();
    private final Map<Object, Reservation> reservations = new LinkedHashMap<>();

    @Override
    public void save(Book book) {
        books.put(book.isbn(), book);
    }

    @Override
    public Optional<Book> findByIsbn(Isbn isbn) {
        return Optional.ofNullable(books.get(isbn));
    }

    @Override
    public void save(Copy copy) {
        copies.put(copy.id(), copy);
    }

    @Override
    public Optional<Copy> findById(CopyId id) {
        return Optional.ofNullable(copies.get(id));
    }

    @Override
    public List<Copy> findCopiesOf(Isbn isbn) {
        return copies.values().stream().filter(c -> c.isbn().equals(isbn)).toList();
    }

    @Override
    public void save(Member member) {
        members.put(member.id(), member);
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        return Optional.ofNullable(members.get(id));
    }

    @Override
    public void save(Loan loan) {
        loans.put(loan.id(), loan);
    }

    @Override
    public Optional<Loan> findById(LoanId id) {
        return Optional.ofNullable(loans.get(id));
    }

    @Override
    public List<Loan> findActiveByMember(MemberId member) {
        return loans.values().stream().filter(l -> l.memberId().equals(member) && l.isActive()).toList();
    }

    @Override
    public void save(Reservation reservation) {
        reservations.put(reservation.id(), reservation);
    }

    @Override
    public List<Reservation> findWaiting(Isbn isbn) {
        return reservations.values().stream()
                .filter(r -> r.isbn().equals(isbn) && r.status() == Reservation.Status.WAITING)
                .sorted(Comparator.comparing(Reservation::createdAt))
                .toList();
    }

    @Override
    public <T> T execute(Supplier<T> work) {
        return work.get();
    }
}
