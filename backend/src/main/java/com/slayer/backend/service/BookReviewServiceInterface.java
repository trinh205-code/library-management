package com.slayer.backend.service;

import com.slayer.backend.payload.dto.BookReviewDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.BookReviewCreateRequest;
import com.slayer.backend.payload.request.BookReviewUpdateRequest;

public interface BookReviewServiceInterface {

    BookReviewDTO createReview(BookReviewCreateRequest request);

    BookReviewDTO updateReview(Long reviewId, BookReviewUpdateRequest request);

    void deleteReview(Long reviewId);

    PageResponse<BookReviewDTO> getReviewsByBookId(Long id, int page, int size);

    boolean hasUserReviewedBook(Long bookId);
}
