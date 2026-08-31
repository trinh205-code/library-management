package com.slayer.backend.controller;

import com.slayer.backend.exception.BookException;
import com.slayer.backend.payload.dto.BookDTO;
import com.slayer.backend.service.impl.BookServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/book")
public class AdminBookController {

    private final BookServiceImpl bookService;

    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO bookDTO) throws BookException {

        BookDTO createBook = bookService.createBook(bookDTO);
        return ResponseEntity.ok(createBook);
    }
}
