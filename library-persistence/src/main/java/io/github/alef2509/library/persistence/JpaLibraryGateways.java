package io.github.alef2509.library.persistence;

import io.github.alef2509.library.application.gateway.BookGateway;
import io.github.alef2509.library.application.gateway.CopyGateway;
import io.github.alef2509.library.application.gateway.LoanGateway;
import io.github.alef2509.library.application.gateway.MemberGateway;
import io.github.alef2509.library.application.gateway.ReservationGateway;
import io.github.alef2509.library.domain.Book;
import io.github.alef2509.library.domain.Copy;
import io.github.alef2509.library.domain.Ids.CopyId;
import io.github.alef2509.library.domain.Ids.LoanId;
import io.github.alef2509.library.domain.Ids.MemberId;
import io.github.alef2509.library.domain.Ids.ReservationId;
import io.github.alef2509.library.domain.Isbn;
import io.github.alef2509.library.domain.Loan;
import io.github.alef2509.library.domain.Member;
import io.github.alef2509.library.domain.Reservation;
import io.github.alef2509.library.persistence.Entities.BookEntity;
import io.github.alef2509.library.persistence.Entities.CopyEntity;
import io.github.alef2509.library.persistence.Entities.LoanEntity;
import io.github.alef2509.library.persistence.Entities.MemberEntity;
import io.github.alef2509.library.persistence.Entities.ReservationEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implements every gateway with Spring Data JPA, mapping entities to domain
 * objects and back. Updates go through the managed entity loaded in the same
 * transaction, so its {@code @Version} still guards against concurrent writers.
 */
@Component
class JpaLibraryGateways implements BookGateway, CopyGateway, MemberGateway, LoanGateway, ReservationGateway {

    private final BookRepository books;
    private final CopyRepository copies;
    private final MemberRepository members;
    private final LoanRepository loans;
    private final ReservationRepository reservations;

    JpaLibraryGateways(BookRepository books, CopyRepository copies, MemberRepository members, LoanRepository loans,
                       ReservationRepository reservations) {
        this.books = books;
        this.copies = copies;
        this.members = members;
        this.loans = loans;
        this.reservations = reservations;
    }

    // ---------- books

    @Override
    public void save(Book book) {
        BookEntity e = books.findById(book.isbn().value()).orElseGet(BookEntity::new);
        e.isbn = book.isbn().value();
        e.title = book.title();
        e.author = book.author();
        books.save(e);
    }

    @Override
    public Optional<Book> findByIsbn(Isbn isbn) {
        return books.findById(isbn.value()).map(e -> new Book(new Isbn(e.isbn), e.title, e.author));
    }

    // ---------- copies

    @Override
    public void save(Copy copy) {
        CopyEntity e = copies.findById(copy.id().value()).orElseGet(CopyEntity::new);
        e.id = copy.id().value();
        e.isbn = copy.isbn().value();
        e.status = copy.status().name();
        e.heldFor = copy.heldFor() == null ? null : copy.heldFor().value();
        e.heldUntil = copy.heldUntil();
        copies.save(e);
    }

    @Override
    public Optional<Copy> findById(CopyId id) {
        return copies.findById(id.value()).map(JpaLibraryGateways::toDomain);
    }

    @Override
    public List<Copy> findCopiesOf(Isbn isbn) {
        return copies.findByIsbn(isbn.value()).stream().map(JpaLibraryGateways::toDomain).toList();
    }

    private static Copy toDomain(CopyEntity e) {
        return new Copy(new CopyId(e.id), new Isbn(e.isbn), Copy.Status.valueOf(e.status),
                e.heldFor == null ? null : new MemberId(e.heldFor), e.heldUntil);
    }

    // ---------- members

    @Override
    public void save(Member member) {
        MemberEntity e = new MemberEntity();
        e.id = member.id().value();
        e.name = member.name();
        e.membership = member.membership().name();
        members.save(e);
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        return members.findById(id.value())
                .map(e -> new Member(new MemberId(e.id), e.name, Member.Membership.valueOf(e.membership)));
    }

    // ---------- loans

    @Override
    public void save(Loan loan) {
        LoanEntity e = loans.findById(loan.id().value()).orElseGet(LoanEntity::new);
        e.id = loan.id().value();
        e.copyId = loan.copyId().value();
        e.memberId = loan.memberId().value();
        e.borrowedOn = loan.borrowedOn();
        e.dueOn = loan.dueOn();
        e.renewals = loan.renewals();
        e.returnedOn = loan.returnedOn();
        e.fine = loan.fine();
        loans.save(e);
    }

    @Override
    public Optional<Loan> findById(LoanId id) {
        return loans.findById(id.value()).map(JpaLibraryGateways::toDomain);
    }

    @Override
    public List<Loan> findActiveByMember(MemberId member) {
        return loans.findByMemberIdAndReturnedOnIsNull(member.value()).stream().map(JpaLibraryGateways::toDomain).toList();
    }

    private static Loan toDomain(LoanEntity e) {
        return new Loan(new LoanId(e.id), new CopyId(e.copyId), new MemberId(e.memberId), e.borrowedOn, e.dueOn,
                e.renewals, e.returnedOn, e.fine);
    }

    // ---------- reservations

    @Override
    public void save(Reservation reservation) {
        ReservationEntity e = reservations.findById(reservation.id().value()).orElseGet(ReservationEntity::new);
        e.id = reservation.id().value();
        e.isbn = reservation.isbn().value();
        e.memberId = reservation.memberId().value();
        e.createdAt = reservation.createdAt();
        e.status = reservation.status().name();
        reservations.save(e);
    }

    @Override
    public List<Reservation> findWaiting(Isbn isbn) {
        return reservations.findByIsbnAndStatusOrderByCreatedAt(isbn.value(), Reservation.Status.WAITING.name())
                .stream()
                .map(e -> new Reservation(new ReservationId(e.id), new Isbn(e.isbn), new MemberId(e.memberId),
                        e.createdAt, Reservation.Status.valueOf(e.status)))
                .toList();
    }
}
