package io.github.alef2509.library.application.usecase;

import io.github.alef2509.library.application.gateway.BookGateway;
import io.github.alef2509.library.application.gateway.CopyGateway;
import io.github.alef2509.library.application.gateway.MemberGateway;
import io.github.alef2509.library.application.gateway.ReservationGateway;
import io.github.alef2509.library.application.gateway.UnitOfWork;
import io.github.alef2509.library.domain.Ids.MemberId;
import io.github.alef2509.library.domain.Isbn;
import io.github.alef2509.library.domain.LibraryException;
import io.github.alef2509.library.domain.Reservation;

import java.time.Clock;
import java.time.LocalDate;

/** Reservations only make sense when every copy is taken; otherwise borrow directly. */
public class ReserveBookInteractor implements ReserveBook {

    private final MemberGateway members;
    private final BookGateway books;
    private final CopyGateway copies;
    private final ReservationGateway reservations;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public ReserveBookInteractor(MemberGateway members, BookGateway books, CopyGateway copies,
                                 ReservationGateway reservations, UnitOfWork unitOfWork, Clock clock) {
        this.members = members;
        this.books = books;
        this.copies = copies;
        this.reservations = reservations;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    @Override
    public Response execute(Request request) {
        Isbn isbn = new Isbn(request.isbn());
        MemberId member = new MemberId(request.memberId());
        LocalDate today = LocalDate.now(clock);
        return unitOfWork.execute(() -> {
            members.findById(member).orElseThrow(() -> new NotFoundException("Member", request.memberId()));
            books.findByIsbn(isbn).orElseThrow(() -> new NotFoundException("Book", isbn));
            if (copies.findCopiesOf(isbn).stream().anyMatch(c -> c.canBeBorrowedBy(member, today))) {
                throw new LibraryException("copy-available", "A copy is available: borrow it instead");
            }
            if (reservations.isWaiting(isbn, member)) {
                throw new LibraryException("already-reserved", "Member is already waiting for this book");
            }
            Reservation reservation = Reservation.place(isbn, member, clock.instant());
            reservations.save(reservation);
            return new Response(reservation.id().value(), reservations.findWaiting(isbn).size());
        });
    }
}
