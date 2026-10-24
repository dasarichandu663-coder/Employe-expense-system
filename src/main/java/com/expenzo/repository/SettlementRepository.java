package com.expenzo.repository;

import com.expenzo.entity.Settlement;
import com.expenzo.entity.User;
import com.expenzo.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByAdvanceId(Long advanceId);
    boolean existsByAdvanceId(Long advanceId);
    List<Settlement> findByUserOrderByCreatedAtDesc(User user);
    List<Settlement> findByStatusOrderByCreatedAtDesc(SettlementStatus status);
    List<Settlement> findAllByOrderByCreatedAtDesc();

    long countByUserAndStatus(User user, SettlementStatus status);
    long countByStatus(SettlementStatus status);
}
