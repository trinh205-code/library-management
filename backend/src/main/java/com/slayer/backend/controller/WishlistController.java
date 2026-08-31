package com.slayer.backend.controller;

import com.slayer.backend.payload.dto.WishlistDTO;
import com.slayer.backend.payload.reponse.ApiResponse;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.service.impl.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/wishlists")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{bookId}")
    public ResponseEntity<?> addToWishlist(
            @PathVariable("bookId") Long bookId,
            @RequestParam(required = false) String notes) {
        try {
            WishlistDTO wishlist = wishlistService.addToWishlist(bookId, notes);
            return new ResponseEntity<>(wishlist, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable("bookId") Long bookId) {
        try {
            wishlistService.removeFromWishlist(bookId);
            return ResponseEntity.ok(new ApiResponse("Book removed from wishlist", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }

    @GetMapping
    public ResponseEntity<?> getWishlists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            PageResponse<WishlistDTO> wishlists = wishlistService.getWishlists(page, size);
            return ResponseEntity.ok(wishlists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), false));
        }
    }
}
