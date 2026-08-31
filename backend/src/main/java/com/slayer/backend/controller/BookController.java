package com.slayer.backend.controller;

import com.slayer.backend.entity.Book;
import com.slayer.backend.exception.BookException;
import com.slayer.backend.payload.dto.BookDTO;
import com.slayer.backend.payload.reponse.ApiResponse;
import com.slayer.backend.payload.reponse.BookStatusResponse;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.BookSearchRequest;
import com.slayer.backend.service.impl.BookServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookServiceImpl bookService;


    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO bookDTO) throws BookException {
        BookDTO createBook = bookService.createBook(bookDTO);
        return ResponseEntity.ok(createBook);
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createBooksBulk(@Valid @RequestBody List<BookDTO> bookDTO) throws BookException {
        List<BookDTO> createBook = bookService.createBooksBulk(bookDTO);
        return ResponseEntity.ok(createBook);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) throws BookException {
        BookDTO bookDTO = bookService.getBookById(id);
        return ResponseEntity.ok(bookDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id,
                                        @RequestBody BookDTO bookDTO) throws BookException {
        try {
            BookDTO updateBook = bookService.updateBook(id, bookDTO);
            return ResponseEntity.ok(updateBook);
        } catch (BookException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse> deleteBook(@PathVariable Long id) throws BookException {
        bookService.deleteBook(id);
        return ResponseEntity.ok(new ApiResponse("Book deleted successfully", true));
    }

    @DeleteMapping("{id}/permanent")
    public ResponseEntity<ApiResponse> deleteHardBook(@PathVariable Long id) throws BookException {
        bookService.hardDeleteBook(id);
        return ResponseEntity.ok(new ApiResponse("Book permanently successfully", true));
    }

    @GetMapping()
    public ResponseEntity<PageResponse<BookDTO>> searchBook(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false, defaultValue = "false") Boolean availableOnly,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection

            ) {

        BookSearchRequest bookSearchRequest = new BookSearchRequest();
        bookSearchRequest.setSearchTerm(searchTerm);
        bookSearchRequest.setGenreId(genreId);
        bookSearchRequest.setAvailableOnly(availableOnly);
        bookSearchRequest.setPage(page);
        bookSearchRequest.setSize(size);
        bookSearchRequest.setSortBy(sortBy);
        bookSearchRequest.setSortDirection(sortDirection);


        PageResponse<BookDTO> books = bookService.searchBooksWithFilters(bookSearchRequest);
        return ResponseEntity.ok(books);
    }


    @PostMapping("/search")
    public ResponseEntity<PageResponse<BookDTO>> advancedSearch(@RequestBody BookSearchRequest searchRequest) {

        PageResponse<BookDTO> books = bookService.searchBooksWithFilters(searchRequest);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<BookDTO>> getTrendingBooks(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(bookService.getTrendingBooks(limit));
    }

    @GetMapping("/random")
    public ResponseEntity<List<BookDTO>> getRandomBooks(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(bookService.getRandomBooks(limit));
    }

    @GetMapping("/most-read")
    public ResponseEntity<List<BookDTO>> getMostReadBooks(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(bookService.getMostReadBooks(limit));
    }

    @GetMapping("/status")
    public ResponseEntity<BookStatusResponse> getBookStats() {
        long totalActive = bookService.getTotalActiveBooks();
        long totalAvailable = bookService.getTotalAvailableBooks();

        BookStatusResponse bookStatusResponse = new BookStatusResponse(totalActive, totalAvailable);

        return ResponseEntity.ok(bookStatusResponse);

    }


}
