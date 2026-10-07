package io.github.alef2509.library.application.gateway;

import io.github.alef2509.library.domain.Ids.MemberId;
import io.github.alef2509.library.domain.Isbn;
import io.github.alef2509.library.domain.Reservation;

import java.util.List;

public interface ReservationGateway {

    void save(Reservation reservation);

    /** Waiting reservations for the book, oldest first. */
    List<Reservation> findWaiting(Isbn isbn);

    default boolean isWaiting(Isbn isbn, MemberId member) {
        return findWaiting(isbn).stream().anyMatch(r -> r.memberId().equals(member));
    }
}
