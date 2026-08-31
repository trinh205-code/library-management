package com.slayer.backend.mapper;

import com.slayer.backend.entity.Wishlist;
import com.slayer.backend.payload.dto.WishlistDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WishlistMapper {

    private final BookMapper bookMapper;

    public WishlistDTO toDTO(Wishlist wishlist) {
        if (wishlist == null) {
            return null;
        }

        return WishlistDTO.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser() != null ? wishlist.getUser().getId() : null)
                .userFullName(wishlist.getUser() != null ? wishlist.getUser().getFullName() : null)
                .book(bookMapper.toDTO(wishlist.getBook()))
                .addedAt(wishlist.getAddedAt())
                .notes(wishlist.getNotes())
                .build();
    }
}
