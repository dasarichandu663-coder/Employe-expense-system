package com.expenzo.dto;

import java.math.BigDecimal;

public class DashboardSummaryDto {
    // 4 Primary Summary Cards requested by Expenzo Dashboard
    private BigDecimal activeAdvanceAmount;
    private long pendingSettlementCount;
    private BigDecimal approvedReimbursementAmount;
    private long directClaimCount;

    // Additional metrics for Manager/Admin oversight
    private long pendingAdvanceCount;
    private BigDecimal totalDisbursedAmount;
    private long registeredEmployeeCount;
    private long totalAdvanceCount;

    public DashboardSummaryDto() {
        this.activeAdvanceAmount = BigDecimal.ZERO;
        this.pendingSettlementCount = 0;
        this.approvedReimbursementAmount = BigDecimal.ZERO;
        this.directClaimCount = 0;
        this.pendingAdvanceCount = 0;
        this.totalDisbursedAmount = BigDecimal.ZERO;
        this.registeredEmployeeCount = 0;
        this.totalAdvanceCount = 0;
    }

    public BigDecimal getActiveAdvanceAmount() { return activeAdvanceAmount; }
    public void setActiveAdvanceAmount(BigDecimal activeAdvanceAmount) { this.activeAdvanceAmount = activeAdvanceAmount; }

    public long getPendingSettlementCount() { return pendingSettlementCount; }
    public void setPendingSettlementCount(long pendingSettlementCount) { this.pendingSettlementCount = pendingSettlementCount; }

    public BigDecimal getApprovedReimbursementAmount() { return approvedReimbursementAmount; }
    public void setApprovedReimbursementAmount(BigDecimal approvedReimbursementAmount) { this.approvedReimbursementAmount = approvedReimbursementAmount; }

    public long getDirectClaimCount() { return directClaimCount; }
    public void setDirectClaimCount(long directClaimCount) { this.directClaimCount = directClaimCount; }

    public long getPendingAdvanceCount() { return pendingAdvanceCount; }
    public void setPendingAdvanceCount(long pendingAdvanceCount) { this.pendingAdvanceCount = pendingAdvanceCount; }

    public BigDecimal getTotalDisbursedAmount() { return totalDisbursedAmount; }
    public void setTotalDisbursedAmount(BigDecimal totalDisbursedAmount) { this.totalDisbursedAmount = totalDisbursedAmount; }

    public long getRegisteredEmployeeCount() { return registeredEmployeeCount; }
    public void setRegisteredEmployeeCount(long registeredEmployeeCount) { this.registeredEmployeeCount = registeredEmployeeCount; }

    public long getTotalAdvanceCount() { return totalAdvanceCount; }
    public void setTotalAdvanceCount(long totalAdvanceCount) { this.totalAdvanceCount = totalAdvanceCount; }
}
