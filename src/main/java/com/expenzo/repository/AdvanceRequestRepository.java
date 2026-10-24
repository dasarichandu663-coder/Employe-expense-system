package com.expenzo.repository;

import com.expenzo.entity.AdvanceRequest;
import com.expenzo.entity.User;
import com.expenzo.enums.AdvanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AdvanceRequestRepository extends JpaRepository<AdvanceRequest, Long> {
    List<AdvanceRequest> findByUserOrderByCreatedAtDesc(User user);
    List<AdvanceRequest> findByUserAndStatusOrderByCreatedAtDesc(User user, AdvanceStatus status);
    List<AdvanceRequest> findByStatusOrderByCreatedAtDesc(AdvanceStatus status);
    List<AdvanceRequest> findAllByOrderByCreatedAtDesc();

    long countByUserAndStatus(User user, AdvanceStatus status);
    long countByStatus(AdvanceStatus status);

    @Query("SELECT COALESCE(SUM(a.disbursedAmount), 0) FROM AdvanceRequest a WHERE a.user = :user AND a.status IN ('DISBURSED', 'SETTLEMENT_SUBMITTED')")
    BigDecimal sumActiveDisbursedByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(a.disbursedAmount), 0) FROM AdvanceRequest a WHERE a.status IN ('DISBURSED', 'SETTLEMENT_SUBMITTED', 'SETTLED')")
    BigDecimal sumTotalDisbursedAll();
}
