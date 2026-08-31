package com.slayer.backend.entity;

import com.slayer.backend.Enums.BookLoanStatus;
import com.slayer.backend.Enums.BookLoanType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Book book;

    private BookLoanType bookLoanType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookLoanStatus status;


    @Column(nullable = false)
    private LocalDateTime checkOutDate;

    private LocalDateTime dueDate;


    private LocalDateTime returnDate;

    @Column(nullable = false)
    private Integer renewalCount = 0;

    @Column(nullable = false)
    private Integer maxRenewals = 2;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Boolean isOverdue = false;

    @Column(nullable = false)
    private Integer overdueDays = 0;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updatedDate;

    public boolean isActive() {
        return status ==BookLoanStatus.CHECKED_OUT
                || status == BookLoanStatus.OVERDUE;
    }

    public boolean canRenew() {
        return status == BookLoanStatus.CHECKED_OUT
                && !isOverdue
                && renewalCount<maxRenewals;
    }
}
