package com.slayer.backend.service.impl;

import com.slayer.backend.entity.Book;
import com.slayer.backend.entity.User;
import com.slayer.backend.entity.Wishlist;
import com.slayer.backend.mapper.WishlistMapper;
import com.slayer.backend.payload.dto.WishlistDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.repository.BookRepository;
import com.slayer.backend.repository.WishlistRepository;
import com.slayer.backend.service.WishlistServiceInterface;
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
public class WishlistService implements WishlistServiceInterface {

    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final WishlistMapper wishlistMapper;

    @Override
    public WishlistDTO addToWishlist(Long bookId, String notes) {
        try {
            User user = userService.getUser();

            Book book = bookRepository.findById(bookId).orElseThrow(
                    () -> new Exception("Book not found with id " + bookId)
            );

            if (wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId)) {
                throw new Exception("Book is already in your wishlist");
            }

            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .book(book)
                    .notes(notes)
                    .build();

            Wishlist savedWishlist = wishlistRepository.save(wishlist);
            return wishlistMapper.toDTO(savedWishlist);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void removeFromWishlist(Long bookId) {
        try {
            User user = userService.getUser();

            Wishlist wishlist = wishlistRepository.findByUserIdAndBookId(user.getId(), bookId)
                    .orElseThrow(() -> new Exception("Wishlist item not found for this book"));

            wishlistRepository.delete(wishlist);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<WishlistDTO> getWishlists(int page, int size) {
        try {
            User user = userService.getUser();

            size = Math.min(100, size);
            size = Math.max(1, size);

            Pageable pageable = PageRequest.of(page, size, Sort.by("addedAt").descending());

            Page<Wishlist> wishlistPage = wishlistRepository.findByUserId(user.getId(), pageable);

            return convertToPageResponse(wishlistPage);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private PageResponse<WishlistDTO> convertToPageResponse(Page<Wishlist> wishlistPage) {
        List<WishlistDTO> wishlistDTOS = wishlistPage.getContent()
                .stream()
                .map(wishlistMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                wishlistDTOS,
                wishlistPage.getNumber(),
                wishlistPage.getSize(),
                wishlistPage.getTotalElements(),
                wishlistPage.getTotalPages(),
                wishlistPage.isLast(),
                wishlistPage.isFirst(),
                wishlistPage.isEmpty()
        );
    }
}
