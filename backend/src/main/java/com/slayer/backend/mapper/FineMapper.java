package com.slayer.backend.mapper;

import com.slayer.backend.Enums.FineStatus;
import com.slayer.backend.entity.Fine;
import com.slayer.backend.payload.dto.FineDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FineMapper {


    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "bookLoan.id", target = "bookLoanId")
    @Mapping(source = "bookLoan.book.title", target = "bookTitle")
    @Mapping(source = "bookLoan.book.isbn", target = "bookIsbn")
    @Mapping(source = "waiveBy.id", target = "waivedByUserId")
    @Mapping(source = "waiveBy.fullName", target = "waivedByUserName")
    @Mapping(source = "processedBy.id", target = "processedByUserName")
    @Mapping(target = "amountPaid", source = ".", qualifiedByName = "calculateAmountPaid")
    @Mapping(target = "amountOutstanding", source = ".", qualifiedByName = "calculateAmountOutstanding")
    FineDTO toDTO(Fine fine);


    @Named("calculateAmountPaid")
    default Long calculateAmountPaid(Fine fine) {
        if (fine.getAmount() == null) return 0L;
        // Nếu đã thanh toán, tiền đã trả = tổng tiền
        if (fine.getStatus() == FineStatus.PAID) {
            return fine.getAmount();
        }
        return 0L;
    }

    @Named("calculateAmountOutstanding")
    default Long calculateAmountOutstanding(Fine fine) {
        if (fine.getAmount() == null) return 0L;
        // Nếu đã thanh toán hoặc được miễn, nợ = 0
        if (fine.getStatus() == FineStatus.PAID || fine.getStatus() == FineStatus.WAIVED) {
            return 0L;
        }
        // Nếu chưa thanh toán, nợ = tổng tiền
        return fine.getAmount();
    }
}