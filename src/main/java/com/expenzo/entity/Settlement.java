package com.expenzo.entity;

import com.expenzo.enums.ReconciliationOutcome;
import com.expenzo.enums.SettlementStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advance_id", nullable = false, unique = true)
    private AdvanceRequest advance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_disbursed_advance", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDisbursedAdvance;

    @Column(name = "total_eligible_expenses", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalEligibleExpenses;

    @Column(name = "remaining_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReconciliationOutcome outcome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementStatus status = SettlementStatus.PENDING_REVIEW;

    @Column(name = "submission_notes", columnDefinition = "TEXT")
    private String submissionNotes;

    @Column(name = "manager_comment", columnDefinition = "TEXT")
    private String managerComment;

    @Column(name = "payment_or_refund_reference", length = 100)
    private String paymentOrRefundReference;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Settlement() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AdvanceRequest getAdvance() { return advance; }
    public void setAdvance(AdvanceRequest advance) { this.advance = advance; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
