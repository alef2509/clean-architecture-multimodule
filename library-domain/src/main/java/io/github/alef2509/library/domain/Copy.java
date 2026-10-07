package io.github.alef2509.library.domain;

import io.github.alef2509.library.domain.Ids.CopyId;
import io.github.alef2509.library.domain.Ids.MemberId;

import java.time.LocalDate;

/**
 * A physical copy of a book. On return, a copy may be put <em>on hold</em> for the
 * first member waiting for that book: only that member can borrow it until the
 * hold expires.
 */
public class Copy {

    public enum Status { AVAILABLE, ON_LOAN, ON_HOLD }

    private final CopyId id;
    private final Isbn isbn;
    private Status status;
    private MemberId heldFor;
    private LocalDate heldUntil;

    public Copy(CopyId id, Isbn isbn, Status status, MemberId heldFor, LocalDate heldUntil) {
        this.id = id;
        this.isbn = isbn;
        this.status = status;
        this.heldFor = heldFor;
        this.heldUntil = heldUntil;
    }

    public static Copy newCopy(Isbn isbn) {
        return new Copy(CopyId.next(), isbn, Status.AVAILABLE, null, null);
    }

    public boolean canBeBorrowedBy(MemberId member, LocalDate today) {
        return switch (status) {
            case AVAILABLE -> true;
            case ON_LOAN -> false;
            case ON_HOLD -> member.equals(heldFor) || today.isAfter(heldUntil); // expired holds lapse
        };
    }

    void lend(MemberId member, LocalDate today) {
        if (!canBeBorrowedBy(member, today)) {
            throw new LibraryException("copy-unavailable", "Copy %s is not available".formatted(id.value()));
        }
        status = Status.ON_LOAN;
        heldFor = null;
        heldUntil = null;
    }

    public void returned() {
        status = Status.AVAILABLE;
    }

    public void holdFor(MemberId member, LocalDate until) {
        status = Status.ON_HOLD;
        heldFor = member;
        heldUntil = until;
    }

    public CopyId id() {
        return id;
    }

    public Isbn isbn() {
        return isbn;
    }

    public Status status() {
        return status;
    }

    public MemberId heldFor() {
        return heldFor;
    }

    public LocalDate heldUntil() {
        return heldUntil;
    }
}
