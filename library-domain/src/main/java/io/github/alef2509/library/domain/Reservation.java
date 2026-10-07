package io.github.alef2509.library.domain;

import io.github.alef2509.library.domain.Ids.MemberId;
import io.github.alef2509.library.domain.Ids.ReservationId;

import java.time.Instant;

/** A member waiting for a book; served first-come, first-served. */
public class Reservation {

    public enum Status { WAITING, FULFILLED }

    private final ReservationId id;
    private final Isbn isbn;
    private final MemberId memberId;
    private final Instant createdAt;
    private Status status;

    public Reservation(ReservationId id, Isbn isbn, MemberId memberId, Instant createdAt, Status status) {
        this.id = id;
        this.isbn = isbn;
        this.memberId = memberId;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static Reservation place(Isbn isbn, MemberId member, Instant now) {
        return new Reservation(ReservationId.next(), isbn, member, now, Status.WAITING);
    }

    public void fulfilled() {
        status = Status.FULFILLED;
    }

    public ReservationId id() {
        return id;
    }

    public Isbn isbn() {
        return isbn;
    }

    public MemberId memberId() {
        return memberId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Status status() {
        return status;
    }
}
