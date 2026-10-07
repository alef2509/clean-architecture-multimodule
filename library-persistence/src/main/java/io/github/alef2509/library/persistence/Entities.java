package io.github.alef2509.library.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entities live here, in the adapter, and nowhere else: the domain objects
 * never carry annotations, no-arg constructors or lazy-loading proxies.
 */
final class Entities {

    private Entities() {
    }

    @Entity
    @Table(name = "books")
    static class BookEntity {
        @Id
        String isbn;
        String title;
        String author;
    }

    @Entity
    @Table(name = "copies")
    static class CopyEntity {
        @Id
        UUID id;
        String isbn;
        String status;
        @Column(name = "held_for")
        UUID heldFor;
        @Column(name = "held_until")
        LocalDate heldUntil;
        @Version
        long version;
    }

    @Entity
    @Table(name = "members")
    static class MemberEntity {
        @Id
        UUID id;
        String name;
        String membership;
    }

    @Entity
    @Table(name = "loans")
    static class LoanEntity {
        @Id
        UUID id;
        @Column(name = "copy_id")
        UUID copyId;
        @Column(name = "member_id")
        UUID memberId;
        @Column(name = "borrowed_on")
        LocalDate borrowedOn;
        @Column(name = "due_on")
        LocalDate dueOn;
        int renewals;
        @Column(name = "returned_on")
        LocalDate returnedOn;
        BigDecimal fine;
        @Version
        long version;
    }

    @Entity
    @Table(name = "reservations")
    static class ReservationEntity {
        @Id
        UUID id;
        String isbn;
        @Column(name = "member_id")
        UUID memberId;
        @Column(name = "created_at")
        Instant createdAt;
        String status;
    }
}
