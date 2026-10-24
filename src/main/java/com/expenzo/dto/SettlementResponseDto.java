package com.expenzo.dto;

import com.expenzo.enums.ReconciliationOutcome;
import com.expenzo.enums.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SettlementResponseDto {
    private Long id;
    private Long advanceId;
    private String advancePurpose;
    private Long userId;
    private String employeeName;
    private String employeeEmail;
    private BigDecimal totalDisbursedAdvance;
    private BigDecimal totalEligibleExpenses;
    private BigDecimal remainingBalance;
    private ReconciliationOutcome outcome;
    private SettlementStatus status;
    private String submissionNotes;
    private String managerComment;
    private String paymentOrRefundReference;
    private LocalDate completedDate;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    public SettlementResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdvanceId() { return advanceId; }
    public void setAdvanceId(Long advanceId) { this.advanceId = advanceId; }

    public String getAdvancePurpose() { return advancePurpose; }
    public void setAdvancePurpose(String advancePurpose) { this.advancePurpose = advancePurpose; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public BigDecimal getTotalDisbursedAdvance() { return totalDisbursedAdvance; }
    public void setTotalDisbursedAdvance(BigDecimal totalDisbursedAdvance) { this.totalDisbursedAdvance = totalDisbursedAdvance; }

    public BigDecimal getTotalEligibleExpenses() { return totalEligibleExpenses; }
    public void setTotalEligibleExpenses(BigDecimal totalEligibleExpenses) { this.totalEligibleExpenses = totalEligibleExpenses; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public ReconciliationOutcome getOutcome() { return outcome; }
    public void setOutcome(ReconciliationOutcome outcome) { this.outcome = outcome; }

    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }

    public String getSubmissionNotes() { return submissionNotes; }
    public void setSubmissionNotes(String submissionNotes) { this.submissionNotes = submissionNotes; }

    public String getManagerComment() { return managerComment; }
    public void setManagerComment(String managerComment) { this.managerComment = managerComment; }

    public String getPaymentOrRefundReference() { return paymentOrRefundReference; }
    public void setPaymentOrRefundReference(String paymentOrRefundReference) { this.paymentOrRefundReference = paymentOrRefundReference; }

    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
