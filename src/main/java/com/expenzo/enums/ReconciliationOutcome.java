package com.expenzo.enums;

/**
 * Outcome of the settlement calculation:
 * - RETURN_DUE: Advance > Approved Expenses (Employee must refund company)
 * - ADDITIONAL_PAYMENT_DUE: Approved Expenses > Advance (Company owes employee reimbursement)
 * - BALANCED: Advance == Approved Expenses (Exact match, zero difference)
 */
public enum ReconciliationOutcome {
    RETURN_DUE,
    ADDITIONAL_PAYMENT_DUE,
    BALANCED
}
