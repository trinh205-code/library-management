package com.slayer.backend.mapper;

import com.slayer.backend.entity.Reservation;
import com.slayer.backend.payload.dto.ReservationDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    // Map User fields
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")

    // Map Book fields
    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "bookTitle")
    @Mapping(source = "book.isbn", target = "bookIsbn")
    @Mapping(source = "book.author", target = "bookAuthor")

    // Ignore computed fields
    @Mapping(target = "isBookAvailable", ignore = true)
    @Mapping(target = "isExpired", ignore = true)
    @Mapping(target = "canBeCancelled", ignore = true)
    @Mapping(target = "hoursUtilExpiry", ignore = true)
    ReservationDTO toDTO(Reservation reservation);

    @AfterMapping
    default void calculateDynamicFields(Reservation reservation, @MappingTarget ReservationDTO dto) {
        if (reservation.getBook() != null) {
            dto.setIsBookAvailable(reservation.getBook().getAvailableCopies() != null
                    && reservation.getBook().getAvailableCopies() > 0);
        }

        if (reservation.getAvailableUntil() != null) {
            dto.setExpired(reservation.getAvailableUntil().isBefore(LocalDateTime.now())
                    && reservation.getStatus() != com.slayer.backend.Enums.ReservationStatus.BORROWED
                    && reservation.getStatus() != com.slayer.backend.Enums.ReservationStatus.CANCELLED);
        }

        // canBeCancelled
        dto.setCanBeCancelled(
                reservation.getStatus() == com.slayer.backend.Enums.ReservationStatus.PENDING
                        || reservation.getStatus() == com.slayer.backend.Enums.ReservationStatus.READY
                        || reservation.getStatus() == com.slayer.backend.Enums.ReservationStatus.USER_CONFIRMED);

        // hoursUtilExpiry
        if (reservation.getAvailableUntil() != null
                && reservation.getStatus() == com.slayer.backend.Enums.ReservationStatus.READY) {
            long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), reservation.getAvailableUntil());
            dto.setHoursUtilExpiry(Math.max(0, hours));
        }
    }
}
