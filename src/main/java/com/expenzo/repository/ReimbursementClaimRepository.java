package com.expenzo.repository;

import com.expenzo.entity.ReimbursementClaim;
import com.expenzo.entity.User;
import com.expenzo.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReimbursementClaimRepository extends JpaRepository<ReimbursementClaim, Long> {
    List<ReimbursementClaim> findByUserOrderByExpenseDateDesc(User user);
    List<ReimbursementClaim> findByUserAndStatusOrderByExpenseDateDesc(User user, ClaimStatus status);
    List<ReimbursementClaim> findByStatusOrderByCreatedAtDesc(ClaimStatus status);
    List<ReimbursementClaim> findAllByOrderByCreatedAtDesc();

    long countByUser(User user);
    long countByStatus(ClaimStatus status);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM ReimbursementClaim c WHERE c.user = :user AND c.status IN ('APPROVED', 'PAID')")
    BigDecimal sumApprovedClaimsByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM ReimbursementClaim c WHERE c.status IN ('APPROVED', 'PAID')")
    BigDecimal sumApprovedClaimsAll();
}
