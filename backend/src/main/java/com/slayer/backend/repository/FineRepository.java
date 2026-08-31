package com.slayer.backend.repository;

import com.slayer.backend.Enums.FineStatus;
import com.slayer.backend.Enums.FineType;
import com.slayer.backend.entity.Fine;
import com.slayer.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine,Long> {

    @Query("""
            SELECT f FROM Fine f
            WHERE (:userId IS NULL OR f.user.id = :userId)
            AND (:status IS NULL OR f.status = :status)
            AND (:type IS NULL OR f.type = :type)
            order by f.createdAt DESC
        """)
    Page<Fine> findAllWithFilters(@Param("userId") Long userId,
                                  @Param("status")FineStatus status,
                                  @Param("type")FineType type,
                                  Pageable pageable
    );


    @Query("SELECT f FROM Fine f WHERE f.user = :user " +
            "AND (:status IS NULL OR f.status = :status) " +
            "AND (:type IS NULL OR f.type = :type) " +
            "ORDER BY f.createdAt DESC")
    List<Fine> findByUserAndOptionalFilters(@Param("user") User user,
                                            @Param("status") FineStatus status,
                                            @Param("type") FineType type);

    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.status != :excludedStatus")
    Long getTotalFinesIssued(@Param("excludedStatus") FineStatus excludedStatus);

}
