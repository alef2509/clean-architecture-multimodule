CREATE TABLE books
(
    isbn   VARCHAR(13) PRIMARY KEY,
    title  VARCHAR(255) NOT NULL,
    author VARCHAR(255)
);

CREATE TABLE copies
(
    id         UUID PRIMARY KEY,
    isbn       VARCHAR(13) NOT NULL REFERENCES books (isbn),
    status     VARCHAR(20) NOT NULL,
    held_for   UUID,
    held_until DATE,
    version    BIGINT      NOT NULL
);

CREATE TABLE members
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    membership VARCHAR(20)  NOT NULL
);

CREATE TABLE loans
(
    id          UUID PRIMARY KEY,
    copy_id     UUID          NOT NULL REFERENCES copies (id),
    member_id   UUID          NOT NULL REFERENCES members (id),
    borrowed_on DATE          NOT NULL,
    due_on      DATE          NOT NULL,
    renewals    INTEGER       NOT NULL,
    returned_on DATE,
    fine        NUMERIC(8, 2) NOT NULL,
    version     BIGINT        NOT NULL
);

-- Last line of defence against two simultaneous loans of the same copy.
CREATE UNIQUE INDEX ux_loans_one_active_per_copy ON loans (copy_id) WHERE returned_on IS NULL;
CREATE INDEX ix_loans_member_active ON loans (member_id) WHERE returned_on IS NULL;

CREATE TABLE reservations
(
    id         UUID PRIMARY KEY,
    isbn       VARCHAR(13) NOT NULL REFERENCES books (isbn),
    member_id  UUID        NOT NULL REFERENCES members (id),
    created_at TIMESTAMPTZ NOT NULL,
    status     VARCHAR(20) NOT NULL
);

CREATE UNIQUE INDEX ux_reservations_one_waiting ON reservations (isbn, member_id) WHERE status = 'WAITING';
