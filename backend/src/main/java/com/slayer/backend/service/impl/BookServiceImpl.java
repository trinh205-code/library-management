package com.slayer.backend.service.impl;

import com.slayer.backend.entity.Book;
import com.slayer.backend.exception.BookException;
import com.slayer.backend.mapper.BookMapper;
import com.slayer.backend.payload.dto.BookDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.BookSearchRequest;
import com.slayer.backend.repository.BookRepository;
import com.slayer.backend.service.BookServiceInterface;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookServiceInterface {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public BookDTO createBook(BookDTO bookDTO) throws BookException {
        if(bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new BookException("book with isbn " + bookDTO.getIsbn() + "already exits");
        }

        Book book = bookMapper.toEntity(bookDTO);

        if (book.getAvailableCopies() != null && book.getTotalCopies() != null
                && book.getAvailableCopies() > book.getTotalCopies()) {
            throw new BookException("Available copies cannot exceed total copies");
        }

        book.isAvailableCopesValid();

        Book savedBook = bookRepository.save(book);

        return bookMapper.toDTO(savedBook);
    }

    @Override
    public List<BookDTO> createBooksBulk(List<BookDTO> bookDTOS) throws BookException {

        List<BookDTO> createdBooks = new ArrayList<>();
        for(BookDTO bookDTO : bookDTOS) {
            BookDTO book = createBook(bookDTO);
            createdBooks.add(book);
        }


        return createdBooks;
    }

    @Override
    public BookDTO getBookById(Long bookId) throws BookException {
        Book book = bookRepository.findById(bookId).orElseThrow(
                ()-> new BookException("book not found")
        );

        return bookMapper.toDTO(book);
    }

    @Override
    public BookDTO getBookByISBN(String isbn) throws BookException {
        Book book = bookRepository.findByIsbn(isbn).orElseThrow(
                ()-> new BookException("book not found")
        );

        return bookMapper.toDTO(book);
    }

    @Override
    public BookDTO updateBook(Long bookId, BookDTO bookDTO) throws BookException {
        Book exitstingBook = bookRepository.findById(bookId).orElseThrow(
                ()-> new BookException("book not found")
        );

        int oldTotal = exitstingBook.getTotalCopies() != null ? exitstingBook.getTotalCopies() : 0;
        int oldAvailable = exitstingBook.getAvailableCopies() != null ? exitstingBook.getAvailableCopies() : 0;
        int loanedCopies = oldTotal - oldAvailable;

        bookMapper.updateEntityFromDTO(bookDTO, exitstingBook);

        int newTotal = exitstingBook.getTotalCopies() != null ? exitstingBook.getTotalCopies() : 0;
        int providedAvailable = exitstingBook.getAvailableCopies() != null ? exitstingBook.getAvailableCopies() : 0;

        if (newTotal != oldTotal && providedAvailable == oldAvailable) {
            int newAvailable = newTotal - loanedCopies;
            if (newAvailable < 0) {
                throw new BookException("Total copies cannot be reduced below the number of currently loaned copies");
            }
            exitstingBook.setAvailableCopies(newAvailable);
        } else if (providedAvailable > newTotal) {
            throw new BookException("Available copies cannot exceed total copies");
        }

        exitstingBook.isAvailableCopesValid();
        Book savedBook = bookRepository.save(exitstingBook);

        return bookMapper.toDTO(savedBook);
    }

    @Override
    public void deleteBook(Long bookId) throws BookException {
        Book exitstingBook = bookRepository.findById(bookId).orElseThrow(
                ()-> new BookException("book not found")
        );
        exitstingBook.setActive(false);
        bookRepository.save(exitstingBook);
    }

    @Override
    public void hardDeleteBook(Long bookId) throws BookException {
        Book exitstingBook = bookRepository.findById(bookId).orElseThrow(
                ()-> new BookException("book not found")
        );
        bookRepository.delete(exitstingBook);
    }


    @Override
    public PageResponse<BookDTO> searchBooksWithFilters(BookSearchRequest searchRequest) {
        Pageable pageable = createPageable(searchRequest.getPage(), searchRequest.getSize(),
                searchRequest.getSortBy(), searchRequest.getSortDirection());


        Page<Book> bookPage = bookRepository.searchBooksWithFilters(
                searchRequest.getSearchTerm(),
                searchRequest.getGenreId(),
                searchRequest.getAvailableOnly() != null ? searchRequest.getAvailableOnly() : false,
                searchRequest.getActiveOnly() != null ? searchRequest.getActiveOnly() : true,
                pageable
        );

        return convertToPageResponse(bookPage);
    }


    @Override
    public long getTotalActiveBooks() {
        return bookRepository.countByActiveTrue();
    }

    @Override
    public long getTotalAvailableBooks() {
        return bookRepository.countAvailableBooks();
    }

    @Override
    public List<BookDTO> getTrendingBooks(int limit) {
        Page<Book> books = bookRepository.getTrendingBooks(PageRequest.of(0, limit));
        return books.getContent().stream().map(bookMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getRandomBooks(int limit) {
        Page<Book> books = bookRepository.getRandomBooks(PageRequest.of(0, limit));
        return books.getContent().stream().map(bookMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getMostReadBooks(int limit) {
        Page<Book> books = bookRepository.getMostReadBooks(PageRequest.of(0, limit));
        return books.getContent().stream().map(bookMapper::toDTO).collect(Collectors.toList());
    }


    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        size=Math.min(size,30);
        size=Math.max(size,1);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending():Sort.by(sortBy).descending();

        return PageRequest.of(page, size, sort);
    }

    private PageResponse<BookDTO> convertToPageResponse(Page<Book> books) {
        List<BookDTO> bookDTOS = books.getContent()
                .stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());

        return  new PageResponse<>(bookDTOS,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isLast(),
                books.isFirst(),
                books.isEmpty());
    }
}
