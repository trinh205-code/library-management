package com.slayer.backend.controller;

import com.slayer.backend.payload.dto.BookReviewDTO;
import com.slayer.backend.payload.reponse.ApiResponse;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.BookReviewCreateRequest;
import com.slayer.backend.payload.request.BookReviewUpdateRequest;
import com.slayer.backend.service.impl.BookReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class BookReviewController {

    private final BookReviewService bookReviewService;

    @PostMapping
    public ResponseEntity<?> createReview(@Valid @RequestBody BookReviewCreateRequest request) {
        try {
            BookReviewDTO review = bookReviewService.createReview(request);
            return new ResponseEntity<>(review, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable("reviewId") Long reviewId,
            @Valid @RequestBody BookReviewUpdateRequest request) {
        try {
            BookReviewDTO review = bookReviewService.updateReview(reviewId, request);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable("reviewId") Long reviewId) {
        try {
            bookReviewService.deleteReview(reviewId);
            return ResponseEntity.ok(new ApiResponse("Review deleted successfully", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getReviewsByBookId(
            @PathVariable("bookId") Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            PageResponse<BookReviewDTO> reviews = bookReviewService.getReviewsByBookId(bookId, page, size);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @GetMapping("/book/{bookId}/check")
    public ResponseEntity<?> checkUserReview(@PathVariable("bookId") Long bookId) {
        try {
            boolean hasReviewed = bookReviewService.hasUserReviewedBook(bookId);
            return ResponseEntity.ok(new ApiResponse("Status checked", hasReviewed));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }
}
