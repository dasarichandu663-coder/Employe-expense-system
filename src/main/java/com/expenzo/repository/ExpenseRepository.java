package com.expenzo.repository;

import com.expenzo.entity.Expense;
import com.expenzo.entity.User;
import com.expenzo.enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByAdvanceIdOrderByExpenseDateDesc(Long advanceId);
    List<Expense> findByAdvanceIdAndStatus(Long advanceId, ExpenseStatus status);
    List<Expense> findByUserOrderByExpenseDateDesc(User user);
    List<Expense> findByStatusOrderByCreatedAtDesc(ExpenseStatus status);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.advance.id = :advanceId AND e.status = 'APPROVED'")
    BigDecimal sumApprovedAmountByAdvanceId(@Param("advanceId") Long advanceId);
}
