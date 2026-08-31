package com.slayer.backend.mapper;

import com.slayer.backend.entity.BookLoan;
import com.slayer.backend.payload.dto.BookLoanDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring")
public interface BookLoanMapper {


    // 1. Map dữ liệu từ object User
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    // 2. Map dữ liệu từ object Book
    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "bookTitle")
    @Mapping(source = "book.author", target = "bookAuthor")
    @Mapping(source = "book.isbn", target = "bookIsbn")
    @Mapping(source = "book.coverImageUrl", target = "bookCoverImage")
    // 3. Map các trường bị lệch tên / lệch chữ hoa chữ thường
    @Mapping(source = "bookLoanType", target = "type")
    @Mapping(source = "checkOutDate", target = "checkoutDate")
    BookLoanDTO toDTO(BookLoan bookLoan);


    @Mapping(source = "type", target = "bookLoanType")
    @Mapping(source = "checkoutDate", target = "checkOutDate")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "book", ignore = true)
    BookLoan toEntity(BookLoanDTO bookLoanDTO);




    @AfterMapping
    default void calculateDynamicFields(BookLoan bookLoan, @MappingTarget BookLoanDTO dto) {
        if (dto.getDueDate() != null) {
            if (dto.getReturnDate() != null) {
                // Nếu đã trả sách rồi thì số ngày còn lại = 0
                dto.setRemainingDays(0);
            } else {
                // Nếu chưa trả: Lấy Hạn trả (dueDate) trừ đi Hôm nay
                long days = ChronoUnit.DAYS.between(LocalDate.now(), dto.getDueDate());
                // Nếu quá hạn (days < 0), thì trả về 0 để không bị số âm
                dto.setRemainingDays((int) Math.max(0, days));
            }
        }
    }
}