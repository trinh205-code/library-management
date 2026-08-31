package com.slayer.backend.service.impl;

import com.slayer.backend.entity.Book;
import com.slayer.backend.entity.BookReview;
import com.slayer.backend.entity.User;
import com.slayer.backend.mapper.BookReviewMapper;
import com.slayer.backend.payload.dto.BookReviewDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.BookReviewCreateRequest;
import com.slayer.backend.payload.request.BookReviewUpdateRequest;
import com.slayer.backend.repository.BookRepository;
import com.slayer.backend.repository.BookReviewRepository;
import com.slayer.backend.service.BookReviewServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookReviewService implements BookReviewServiceInterface {

    private final BookReviewRepository bookReviewRepository;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final BookReviewMapper bookReviewMapper;

    @Override
    public BookReviewDTO createReview(BookReviewCreateRequest request) {
        try {
            User user = userService.getUser();

            Book book = bookRepository.findById(request.getBookId()).orElseThrow(
                    () -> new Exception("Book not found with id " + request.getBookId())
            );

            if (bookReviewRepository.existsByUserIdAndBookId(user.getId(), book.getId())) {
                throw new Exception("You have already reviewed this book");
            }

            BookReview review = BookReview.builder()
                    .user(user)
                    .book(book)
                    .rating(request.getRating())
                    .reviewText(request.getReviewText())
                    .title(request.getTitle())
                    .build();

            BookReview savedReview = bookReviewRepository.save(review);
            updateBookRatingInfo(book);
            return bookReviewMapper.toDTO(savedReview);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public BookReviewDTO updateReview(Long reviewId, BookReviewUpdateRequest request) {
        try {
            User user = userService.getUser();

            BookReview review = bookReviewRepository.findById(reviewId).orElseThrow(
                    () -> new Exception("Review not found with id " + reviewId)
            );

            if (!review.getUser().getId().equals(user.getId())) {
                throw new Exception("You can only update your own reviews");
            }

            if (request.getRating() != null) {
                review.setRating(request.getRating());
            }
            if (request.getReviewText() != null) {
                review.setReviewText(request.getReviewText());
            }
            if (request.getTitle() != null) {
                review.setTitle(request.getTitle());
            }

            BookReview updatedReview = bookReviewRepository.save(review);
            updateBookRatingInfo(review.getBook());
            return bookReviewMapper.toDTO(updatedReview);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteReview(Long reviewId) {
        try {
            User user = userService.getUser();

            BookReview review = bookReviewRepository.findById(reviewId).orElseThrow(
                    () -> new Exception("Review not found with id " + reviewId)
            );

            if (!review.getUser().getId().equals(user.getId())) {
                throw new Exception("You can only delete your own reviews");
            }

            Book book = review.getBook();
            bookReviewRepository.delete(review);
            updateBookRatingInfo(book);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<BookReviewDTO> getReviewsByBookId(Long bookId, int page, int size) {
        try {
            size = Math.min(100, size);
            size = Math.max(1, size);

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<BookReview> reviewPage = bookReviewRepository.findByBookId(bookId, pageable);

            return convertToPageResponse(reviewPage);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private PageResponse<BookReviewDTO> convertToPageResponse(Page<BookReview> reviewPage) {
        List<BookReviewDTO> reviewDTOs = reviewPage.getContent()
                .stream()
                .map(bookReviewMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                reviewDTOs,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isLast(),
                reviewPage.isFirst(),
                reviewPage.isEmpty()
        );
    }

    @Override
    public boolean hasUserReviewedBook(Long bookId) {
        try {
            User user = userService.getUser();
            return bookReviewRepository.existsByUserIdAndBookId(user.getId(), bookId);
        } catch (Exception e) {
            return false;
        }
    }

    private void updateBookRatingInfo(Book book) {
        Double avgRating = bookReviewRepository.getAverageRatingByBookId(book.getId());
        Long totalRatings = bookReviewRepository.countByBookId(book.getId());
        
        book.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        book.setTotalRatings(totalRatings != null ? totalRatings.intValue() : 0);
        
        bookRepository.save(book);
    }
}
