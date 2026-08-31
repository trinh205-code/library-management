package com.slayer.backend.service.impl;

import com.slayer.backend.Enums.BookLoanStatus;
import com.slayer.backend.Enums.BookLoanType;
import com.slayer.backend.entity.Book;
import com.slayer.backend.entity.BookLoan;
import com.slayer.backend.entity.User;
import com.slayer.backend.exception.BookException;
import com.slayer.backend.mapper.BookLoanMapper;
import com.slayer.backend.payload.dto.BookLoanDTO;
import com.slayer.backend.payload.dto.SubscriptionDTO;
import com.slayer.backend.payload.reponse.PageResponse;
import com.slayer.backend.payload.request.BookLoanSearchRequest;
import com.slayer.backend.payload.request.CheckinRequest;
import com.slayer.backend.payload.request.CheckoutRequest;
import com.slayer.backend.payload.request.RenewalRequest;
import com.slayer.backend.repository.BookLoanRepository;
import com.slayer.backend.repository.BookRepository;
import com.slayer.backend.repository.BookReviewRepository;
import com.slayer.backend.service.BookLoanServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookLoanServiceImpl implements BookLoanServiceInterface {

    private final BookLoanRepository bookLoanRepository;
    private final UserService userService;
    private final SubscriptionServiceImpl subscriptionService;
    private final BookRepository bookRepository;
    private final BookLoanMapper bookLoanMapper;
    private final BookReviewRepository bookReviewRepository;

    @Override
    public BookLoanDTO checkoutBook(CheckoutRequest checkoutRequest) throws Exception {
        User user = userService.getUser();

        return checkoutBookForUser(user.getId(), checkoutRequest);
    }

    @Override
    public BookLoanDTO checkoutBookForUser(Long userId, CheckoutRequest checkoutRequest) throws Exception {

        User user = userService.findById(userId);

        SubscriptionDTO subscription = subscriptionService.getUsersActiveSubscription(user.getId());

        Book book = bookRepository.findById(checkoutRequest.getBookId()).orElseThrow(
                ()-> new Exception("book not found with id " + checkoutRequest.getBookId())
        );

        if(!book.getActive()) {
            throw new Exception("book is not active");
        }
        if(book.getAvailableCopies()<=0) {
            throw new Exception("book is not available");
        }

        if(bookLoanRepository.hasActiveCheckout(userId, book.getId())) {
            throw  new Exception("book is already checkout");
        }

        // check user is active checkout limit
        long activeCheckouts = bookLoanRepository.countActiveBookLoansByUserId(userId);
        int maxBooksAllowed = subscription.getMaxBooksAllowed();

        if(activeCheckouts>=maxBooksAllowed) {
            throw new Exception("you have reached the max books allowed");
        }

        long overdueCount = bookLoanRepository.countOverdueBookLoansByUserId(userId);

        if(overdueCount>0) {
            throw new Exception("first return old overdue book");
        }

        BookLoan bookLoan = BookLoan.builder()
                .user(user)
                .book(book)
                .bookLoanType(BookLoanType.CHECKOUT)
                .status(BookLoanStatus.CHECKED_OUT)
                .checkOutDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(checkoutRequest.getCheckoutDays()))
                .renewalCount(0)
                .maxRenewals(2)
                .notes(checkoutRequest.getNotes())
                .isOverdue(false)
                .overdueDays(0)
                .build();

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        book.setBorrowCount(book.getBorrowCount() != null ? book.getBorrowCount() + 1 : 1);
        bookRepository.save(book);

        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);

        BookLoanDTO responseDTO = bookLoanMapper.toDTO(savedBookLoan);

        responseDTO.setRemainingDays(checkoutRequest.getCheckoutDays());

        return responseDTO;
    }

    @Override
    public BookLoanDTO checkinBook(CheckinRequest checkinRequest) throws Exception {

        BookLoan bookLoan = bookLoanRepository.findById(checkinRequest.getBookLoanId()).orElseThrow(
                ()-> new Exception("bookLoan not found")
        );

        if(!bookLoan.isActive()) {
            throw new Exception("book is not active");
        }
        bookLoan.setReturnDate(LocalDateTime.now());

        BookLoanStatus condition = checkinRequest.getCondition();
        if(condition == null) {
            condition = BookLoanStatus.RETURNED;
        }

        bookLoan.setStatus(condition);


        bookLoan.setOverdueDays(0);
        bookLoan.setIsOverdue(false);
        bookLoan.setNotes("book returned by user");

        if(condition!=BookLoanStatus.LOST) {
            Book book = bookLoan.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        }


        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);

        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public BookLoanDTO renewCheckout(RenewalRequest renewalRequest) throws Exception {

        BookLoan bookLoan = bookLoanRepository.findById(renewalRequest.getBookLoanId()).orElseThrow(
                ()-> new Exception("bookLoan not found")
        );

        if(!bookLoan.canRenew()) {
            throw new BookException("book can not be renewed");
        }

        bookLoan.setDueDate(bookLoan.getDueDate().plusDays(renewalRequest.getExtensionDays()));

        bookLoan.setRenewalCount(bookLoan.getRenewalCount() + 1);

        bookLoan.setNotes("book renewed by user");

        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);

        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status, int page, int size) throws Exception {
        User user = userService.getUser();
        Page<BookLoan> bookLoanPage;

        if(status!=null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").descending());
            bookLoanPage = bookLoanRepository.findByStatusAndUser(status, user, pageable);
        } else {

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            bookLoanPage = bookLoanRepository.findByUserId(user.getId(), pageable);
        }

        PageResponse<BookLoanDTO> response = convertToPageResponse(bookLoanPage);
        
        for (BookLoanDTO dto : response.getContent()) {
            dto.setHasReviewed(bookReviewRepository.existsByUserIdAndBookId(user.getId(), Long.valueOf(dto.getBookId())));
        }
        
        return response;
    }

    @Override
    public PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest request) throws Exception {

        Pageable pageable = createPageable(
                request.getPage(),
                request.getSize(),
                request.getSortBy(),
                request.getSortDirection()
        );

        Page<BookLoan> bookLoanPage;

        if(Boolean.TRUE.equals(request.getOverdueOnly())) {
            bookLoanPage = bookLoanRepository.findOverdueBookLoans(LocalDateTime.now(), pageable);
        } else if (request.getUserId() != null) {
            bookLoanPage = bookLoanRepository.findByUserId(request.getUserId(), pageable);
        } else if(request.getBookId() != null) {
            bookLoanPage = bookLoanRepository.findByBookId(request.getBookId(), pageable);
        } else if(request.getStatus() != null) {
            bookLoanPage = bookLoanRepository.findByStatus(request.getStatus(), pageable);
        } else if(request.getStartDate() != null && request.getEndDate() != null) {

            bookLoanPage = bookLoanRepository.findBookLoansByDateRange(
                    request.getStartDate(),
                    request.getEndDate(),
                    pageable);

        } else {
            bookLoanPage = bookLoanRepository.findAll(pageable);
        }

        return convertToPageResponse(bookLoanPage);
    }

    @Override
    public int updateOverdueBookLoan() {
        Pageable pageable = PageRequest.of(0, 1000);
        Page<BookLoan> overduePage = bookLoanRepository.findOverdueBookLoans(LocalDateTime.now(), pageable);

        int updateCount = 0;
        for(BookLoan bookLoan : overduePage.getContent()) {
            if(bookLoan.getStatus().equals(BookLoanStatus.CHECKED_OUT)) {
                bookLoan.setStatus(BookLoanStatus.OVERDUE);
                bookLoan.setIsOverdue(true);

                int overdueDays = calculateOverdueDate(bookLoan.getDueDate(), LocalDateTime.now());
                bookLoan.setOverdueDays(overdueDays);
                bookLoanRepository.save(bookLoan);
                updateCount++;
            }
        }

        return updateCount;
    }


    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        size = Math.min(100, size);
        size = Math.max(1, size);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(page, size, sort);
    }

    private PageResponse<BookLoanDTO> convertToPageResponse(Page<BookLoan> bookLoanPage) {
        List<BookLoanDTO> bookLoanDTOS = bookLoanPage.getContent()
                .stream()
                .map(bookLoanMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                bookLoanDTOS,
                bookLoanPage.getNumber(),
                bookLoanPage.getSize(),
                bookLoanPage.getTotalElements(),
                bookLoanPage.getTotalPages(),
                bookLoanPage.isLast(),
                bookLoanPage.isFirst(),
                bookLoanPage.isEmpty()
        );
    }

    public  int calculateOverdueDate(LocalDateTime dueDate, LocalDateTime today) {

        if(today.isBefore(dueDate) || today.equals(dueDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, today);
    }


}
