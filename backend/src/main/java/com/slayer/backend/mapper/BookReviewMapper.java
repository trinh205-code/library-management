package com.slayer.backend.mapper;

import com.slayer.backend.entity.BookReview;
import com.slayer.backend.payload.dto.BookReviewDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookReviewMapper {

    // Map User fields
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")

    // Map Book fields
    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "bookTitle")
    BookReviewDTO toDTO(BookReview bookReview);
}
