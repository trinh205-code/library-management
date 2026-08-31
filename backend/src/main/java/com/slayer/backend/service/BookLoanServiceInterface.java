package com.slayer.backend.service;

import com.slayer.backend.Enums.BookLoanStatus;
import com.slayer.backend.payload.dto.BookLoanDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.BookLoanSearchRequest;
import com.slayer.backend.payload.request.CheckinRequest;
import com.slayer.backend.payload.request.CheckoutRequest;
import com.slayer.backend.payload.request.RenewalRequest;

public interface BookLoanServiceInterface {

    BookLoanDTO checkoutBook(CheckoutRequest checkoutRequest) throws Exception;

    BookLoanDTO checkoutBookForUser(Long userId, CheckoutRequest checkoutRequest) throws Exception;

    BookLoanDTO checkinBook(CheckinRequest checkinRequest) throws Exception;

    BookLoanDTO renewCheckout(RenewalRequest renewalRequest) throws Exception;

    PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status, int page, int size) throws Exception;

    PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest request) throws Exception;

    int updateOverdueBookLoan();
}
