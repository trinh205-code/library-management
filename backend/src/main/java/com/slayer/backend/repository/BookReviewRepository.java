package com.slayer.backend.repository;

import com.slayer.backend.entity.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Long> {

    Page<BookReview> findByBookId(Long bookId, Pageable pageable);

    Optional<BookReview> findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(r.rating) FROM BookReview r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(@org.springframework.data.repository.query.Param("bookId") Long bookId);

    Long countByBookId(Long bookId);
}
