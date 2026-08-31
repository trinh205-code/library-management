package com.slayer.backend.entity;

import com.slayer.backend.Enums.FineStatus;
import com.slayer.backend.Enums.FineType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    private BookLoan bookLoan;

    private FineType type;

    @Column(nullable = false)
    private Long amount;

    private FineStatus status;

    @Column(length = 500)
    private String reason;

    @Column(length = 1000)
    private String note;

    private LocalDateTime waivedAt;

    @ManyToOne
    private User waiveBy;

    @Column(length = 500)
    private String waiverReason;

    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private User processedBy;

    @Column(length = 100)
    private String transactionId;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public void applyPayment(Long paymentAmount) {
        if(paymentAmount == null || paymentAmount <= 0) {
            throw new IllegalArgumentException("payment amount must be greater than 0");
        }

            this.status = FineStatus.PAID;
            this.paidAt = LocalDateTime.now();
    }

    public void waive(User admin, String reason) {
        this.status = FineStatus.WAIVED;
        this.waiveBy = admin;
        this.waivedAt = LocalDateTime.now();
        this.waiverReason = reason;
    }

}
