package com.expenzo.enums;

/**
 * Lifecycle states of a Cash/Travel Advance Request:
 * 1. PENDING: Submitted by employee, awaiting manager approval.
 * 2. APPROVED: Approved by manager, awaiting actual cash disbursement.
 * 3. DISBURSED: Manager recorded actual payout to employee; employee can now spend & settle.
 * 4. REJECTED: Declined by manager with justification comments.
 * 5. SETTLEMENT_SUBMITTED: Employee submitted reconciliation with actual bills.
 * 6. SETTLED: Final reconciliation audit complete, return/payout verified and closed.
 */
public enum AdvanceStatus {
    PENDING,
    APPROVED,
    DISBURSED,
    REJECTED,
    SETTLEMENT_SUBMITTED,
    SETTLED
}
