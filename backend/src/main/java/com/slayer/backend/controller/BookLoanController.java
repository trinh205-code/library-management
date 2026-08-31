package com.slayer.backend.controller;

import com.slayer.backend.Enums.BookLoanStatus;
import com.slayer.backend.exception.BookException;
import com.slayer.backend.exception.UserException;
import com.slayer.backend.payload.dto.BookLoanDTO;
import com.slayer.backend.payload.reponse.ApiResponse;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.BookLoanSearchRequest;
import com.slayer.backend.payload.request.CheckinRequest;
import com.slayer.backend.payload.request.CheckoutRequest;
import com.slayer.backend.payload.request.RenewalRequest;
import com.slayer.backend.service.impl.BookLoanServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/book-loans")
public class BookLoanController {
    private final BookLoanServiceImpl bookLoanService;


    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutBook(@Valid @RequestBody CheckoutRequest checkoutRequest) {

        try {
            BookLoanDTO bookLoan = bookLoanService.checkoutBook(checkoutRequest);

            return  new ResponseEntity<>(bookLoan, HttpStatus.CREATED);
        } catch (Exception e ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), false));
        }
    }

    @PostMapping("/checkout/user/{userId}")
    public ResponseEntity<?> checkoutBookForUser(@PathVariable("userId") Long userId, @Valid @RequestBody CheckoutRequest checkoutRequest) throws Exception {

        BookLoanDTO bookLoanDTO = bookLoanService.checkoutBookForUser(userId, checkoutRequest);

        return new ResponseEntity<>(bookLoanDTO, HttpStatus.CREATED);
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> checkin(@Valid @RequestBody CheckinRequest checkinRequest) throws Exception {

        BookLoanDTO bookLoanDTO = bookLoanService.checkinBook(checkinRequest);

        return new ResponseEntity<>(bookLoanDTO, HttpStatus.CREATED);
    }

    @PostMapping("/renew")
    public ResponseEntity<?> renew(@Valid @RequestBody RenewalRequest renewalRequest) throws Exception {

        BookLoanDTO bookLoanDTO = bookLoanService.renewCheckout(renewalRequest);

        return new ResponseEntity<>(bookLoanDTO, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyBookLoans(
            @RequestParam(required = false)BookLoanStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws Exception {

        PageResponse<BookLoanDTO> bookLoans = bookLoanService.getMyBookLoans(status, page, size);

        return ResponseEntity.ok(bookLoans);
    }

    @PostMapping("/search")
    public ResponseEntity<?> getAllBookLoans(@RequestBody BookLoanSearchRequest searchRequest) throws Exception {

        PageResponse<BookLoanDTO> bookLoans = bookLoanService.getBookLoans(searchRequest);
        return ResponseEntity.ok(bookLoans);
    }

    @PostMapping("/admin/update-overdue")
    public ResponseEntity<?> updateOverdueBookLoans() {

        int updateCount = bookLoanService.updateOverdueBookLoan();
        return new ResponseEntity<>(updateCount, HttpStatus.OK);
    }
}
