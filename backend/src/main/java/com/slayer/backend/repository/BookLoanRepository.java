package com.slayer.backend.repository;

import com.slayer.backend.Enums.BookLoanStatus;
import com.slayer.backend.entity.BookLoan;
import com.slayer.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface BookLoanRepository extends JpaRepository<BookLoan,Long> {

    Page<BookLoan> findByUserId(Long userId, Pageable pageable);
    Page<BookLoan> findByBookId(Long bookId, Pageable pageable);
    Page<BookLoan> findByStatus(BookLoanStatus bookLoanStatus, Pageable pageable);

    @Query("select case when count(bl) > 0 then true else false end from BookLoan bl " +
            " where bl.user.id =:userId and bl.book.id=:bookId " +
            " and (bl.status = 'CHECKED_OUT' OR bl.status = 'OVERDUE')"
    )
    boolean hasActiveCheckout(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId
    );

    @Query("select count(bl) from BookLoan bl where bl.user.id = :userId " +
            " and (bl.status = 'CHECKED_OUT' OR bl.status = 'OVERDUE')"
    )
    long countActiveBookLoansByUserId(@Param("userId") Long userId);

    @Query("select count(bl) from BookLoan bl where bl.user.id = :userId " +
            " and bl.status = 'OVERDUE' "
    )
    long countOverdueBookLoansByUserId(@Param("userId") Long userId);

    @Query("select count(bl) from BookLoan bl where bl.user.id = :userId " +
            " and bl.status = 'RETURNED' "
    )
    long countReturnedBooksByUserId(@Param("userId") Long userId);

    Page<BookLoan> findByStatusAndUser(BookLoanStatus status, User user, Pageable pageable);

    @Query(" select bl from BookLoan bl where bl.dueDate < :currentDate " +
            " and (bl.status = 'CHECKED_OUT' OR bl.status = 'OVERDUE')")
    Page<BookLoan> findOverdueBookLoans(@Param("currentDate")LocalDateTime currentDate, Pageable pageable);


    @Query(" select bl from BookLoan  bl where bl.checkOutDate between :startDate AND :endDate ")
    Page<BookLoan> findBookLoansByDateRange(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);

}
