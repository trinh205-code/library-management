package com.slayer.backend.repository;

import com.slayer.backend.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book,Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

//    @Query("select b from Book b where" +
//            ":searchTerm is null OR " +
//            "lower(b.title) like lower(concat ('%', :searchTerm, '%')) OR " +
//            "lower(b.author) like lower(concat ('%', :searchTerm, '%')) OR " +
//            "lower(b.isbn) like lower(concat ('%', :searchTerm, '%')) OR " +
//            "(:genreId is null or b.genre.id=:genreId) AND " +
//            "(:availableOnly = false OR b.availableCopies>0 ) AND " +
//            "b.active = true"
//
//    )

    @Query("""
        select b from Book b
        where (
            :searchTerm is null OR 
            lower(b.title) like lower(concat('%', :searchTerm, '%')) OR 
            lower(b.author) like lower(concat('%', :searchTerm, '%')) OR 
            lower(b.isbn) like lower(concat('%', :searchTerm, '%'))
        )
        and (:genreId is null or b.genre.id = :genreId)
        and (:availableOnly = false OR b.availableCopies > 0)
        and (:activeOnly is null OR :activeOnly = false OR b.active = true)
    """)
    Page<Book> searchBooksWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("genreId") Long genreId,
            @Param("availableOnly") boolean availableOnly,
            @Param("activeOnly") Boolean activeOnly,
            Pageable pageable
    );

    @Query("SELECT b FROM Book b WHERE b.active = true ORDER BY (b.totalCopies - b.availableCopies) DESC")
    Page<Book> getTrendingBooks(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.active = true ORDER BY FUNCTION('RAND')")
    Page<Book> getRandomBooks(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.active = true ORDER BY COALESCE(b.borrowCount, 0) DESC")
    Page<Book> getMostReadBooks(Pageable pageable);

    long countByActiveTrue();

    @Query("select count(b) from Book b where b.availableCopies>0 and b.active=true")
    long countAvailableBooks();
}
