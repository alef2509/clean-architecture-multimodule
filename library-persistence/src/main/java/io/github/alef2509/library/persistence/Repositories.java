package io.github.alef2509.library.persistence;

import io.github.alef2509.library.persistence.Entities.BookEntity;
import io.github.alef2509.library.persistence.Entities.CopyEntity;
import io.github.alef2509.library.persistence.Entities.LoanEntity;
import io.github.alef2509.library.persistence.Entities.MemberEntity;
import io.github.alef2509.library.persistence.Entities.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface BookRepository extends JpaRepository<BookEntity, String> {
}

interface CopyRepository extends JpaRepository<CopyEntity, UUID> {
    List<CopyEntity> findByIsbn(String isbn);
}

interface MemberRepository extends JpaRepository<MemberEntity, UUID> {
}

interface LoanRepository extends JpaRepository<LoanEntity, UUID> {
    List<LoanEntity> findByMemberIdAndReturnedOnIsNull(UUID memberId);
}

interface ReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    List<ReservationEntity> findByIsbnAndStatusOrderByCreatedAt(String isbn, String status);
}
