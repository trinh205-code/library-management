package com.slayer.backend.service;

import com.slayer.backend.exception.BookException;
import com.slayer.backend.payload.dto.BookDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.BookSearchRequest;

import java.util.List;

public interface BookServiceInterface {

    BookDTO createBook(BookDTO bookDTO) throws BookException;
    List<BookDTO> createBooksBulk(List<BookDTO> bookDTOs) throws BookException;
    BookDTO getBookById(Long bookId) throws BookException;
    BookDTO getBookByISBN(String isbn) throws BookException;
    BookDTO updateBook(Long bookId,BookDTO bookDTO) throws BookException;
    void deleteBook(Long bookId) throws BookException;
    void hardDeleteBook(Long bookId) throws BookException;

    PageResponse<BookDTO> searchBooksWithFilters(BookSearchRequest searchRequest);

    long getTotalActiveBooks();

    long getTotalAvailableBooks();

    List<BookDTO> getTrendingBooks(int limit);
    List<BookDTO> getRandomBooks(int limit);
    List<BookDTO> getMostReadBooks(int limit);

}
