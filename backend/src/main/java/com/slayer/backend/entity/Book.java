package com.slayer.backend.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @JoinColumn(nullable = false)
    @ManyToOne
    private Genre genre;

    private String publisher;
    private LocalDate publishedDate;
    private String language;
    private Integer pages;
    @Column(length = 2000)
    private String description;
    private BigDecimal price;
    private String coverImageUrl;

    @Column(nullable = false)
    private Integer totalCopies;

    @Column(nullable = false)
    private Integer availableCopies;

    @Column(nullable = false)
    private Boolean active=true;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "double precision default 0.0")
    private Double averageRating = 0.0;

    @Column(columnDefinition = "integer default 0")
    private Integer totalRatings = 0;

    @Column(columnDefinition = "integer default 0")
    private Integer borrowCount = 0;

    @AssertTrue(message = "Available copies cannot exceed total copies")
    public boolean isAvailableCopesValid() {
        if(totalCopies==null || availableCopies ==null) {
            return true;
        }
        return availableCopies<=totalCopies;
    }
}
