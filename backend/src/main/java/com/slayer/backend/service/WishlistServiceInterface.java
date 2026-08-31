package com.slayer.backend.service;

import com.slayer.backend.payload.dto.WishlistDTO;
import com.slayer.backend.payload.reponse.PageResponse;

public interface WishlistServiceInterface {

    WishlistDTO addToWishlist(Long bookId, String notes);
    void removeFromWishlist(Long bookId);
    PageResponse<WishlistDTO> getWishlists(int page, int size);
}
