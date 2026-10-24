package com.expenzo.service;

import com.expenzo.dto.ReconciliationSubmitDto;
import com.expenzo.dto.ReviewRequestDto;
import com.expenzo.dto.SettlementResponseDto;
import com.expenzo.entity.AdvanceRequest;
import com.expenzo.entity.Expense;
import com.expenzo.entity.Settlement;
import com.expenzo.entity.User;
import com.expenzo.enums.AdvanceStatus;
import com.expenzo.enums.ExpenseStatus;
import com.expenzo.enums.ReconciliationOutcome;
import com.expenzo.enums.SettlementStatus;
import com.expenzo.exception.BadRequestException;
import com.expenzo.exception.ResourceNotFoundException;
import com.expenzo.exception.UnauthorizedException;
import com.expenzo.repository.AdvanceRequestRepository;
import com.expenzo.repository.ExpenseRepository;
import com.expenzo.repository.SettlementRepository;
import com.expenzo.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final AdvanceRequestRepository advanceRepository;
    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public SettlementService(SettlementRepository settlementRepository,
                             AdvanceRequestRepository advanceRepository,
                             ExpenseRepository expenseRepository,
                             UserService userService) {
        this.settlementRepository = settlementRepository;
        this.advanceRepository = advanceRepository;
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    /**
     * Submits an advance for settlement and reconciliation.
     * Calculates the exact financial balance using Java BigDecimal:
     * Balance = Disbursed Advance - Approved Eligible Expenses
     *
     * Note: Pending and Rejected expenses are strictly excluded from eligible approved totals.
     */
    @Transactional
    public SettlementResponseDto submitReconciliation(ReconciliationSubmitDto dto) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        User user = userService.getUserByEmail(currentUserEmail);

        AdvanceRequest advance = advanceRepository.findById(dto.getAdvanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Advance request not found with ID: " + dto.getAdvanceId()));

        if (!advance.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only submit reconciliation for your own advances.");
        }

        if (advance.getStatus() != AdvanceStatus.DISBURSED && advance.getStatus() != AdvanceStatus.APPROVED) {
            throw new BadRequestException("Only active disbursed advances can be settled. Current status: " + advance.getStatus());
        }

        // Determine actual disbursed amount
        BigDecimal disbursed = advance.getDisbursedAmount();
        if (disbursed == null || disbursed.compareTo(BigDecimal.ZERO) <= 0) {
            disbursed = advance.getRequestedAmount();
        }

        // Calculate total eligible expenses (ONLY APPROVED status counted!)
        List<Expense> allExpenses = expenseRepository.findByAdvanceIdOrderByExpenseDateDesc(advance.getId());
        BigDecimal totalEligibleApprovedExpenses = allExpenses.stream()
                .filter(e -> e.getStatus() == ExpenseStatus.APPROVED)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // If there are expenses still pending manager approval, notify the employee
        long pendingExpenseCount = allExpenses.stream().filter(e -> e.getStatus() == ExpenseStatus.PENDING).count();

        // Calculate remaining balance: Disbursed Advance - Total Approved Expenses
        BigDecimal remainingBalance = disbursed.subtract(totalEligibleApprovedExpenses);

        ReconciliationOutcome outcome;
        int comp = remainingBalance.compareTo(BigDecimal.ZERO);
        if (comp > 0) {
            outcome = ReconciliationOutcome.RETURN_DUE; // Employee must return unused cash
        } else if (comp < 0) {
            outcome = ReconciliationOutcome.ADDITIONAL_PAYMENT_DUE; // Company reimburses employee difference
        } else {
            outcome = ReconciliationOutcome.BALANCED; // Zero difference
        }

        Settlement settlement = settlementRepository.findByAdvanceId(advance.getId())
                .orElse(new Settlement());

        settlement.setAdvance(advance);
        settlement.setUser(user);
        settlement.setTotalDisbursedAdvance(disbursed);
        settlement.setTotalEligibleExpenses(totalEligibleApprovedExpenses);
        settlement.setRemainingBalance(remainingBalance);
        settlement.setOutcome(outcome);
        settlement.setStatus(SettlementStatus.PENDING_REVIEW);

        String notes = dto.getSubmissionNotes() != null ? dto.getSubmissionNotes().trim() : "";
        if (pendingExpenseCount > 0) {
            notes += " (Note: " + pendingExpenseCount + " expense lines are pending manager approval).";
        }
        settlement.setSubmissionNotes(notes);

        Settlement saved = settlementRepository.save(settlement);

        // Update advance status to track ongoing settlement
        advance.setStatus(AdvanceStatus.SETTLEMENT_SUBMITTED);
        advanceRepository.save(advance);

        return mapToDto(saved);
    }

    public List<SettlementResponseDto> getMySettlements() {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        User user = userService.getUserByEmail(currentUserEmail);
        return settlementRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<SettlementResponseDto> getAllSettlements() {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers can view all staff settlements.");
        }
        return settlementRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public SettlementResponseDto reviewSettlement(Long settlementId, ReviewRequestDto reviewDto) {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers can review settlements.");
        }

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with ID: " + settlementId));

        User reviewer = userService.getUserByEmail(SecurityUtils.getCurrentUserEmail());

        if ("APPROVE".equalsIgnoreCase(reviewDto.getDecision())) {
            settlement.setStatus(SettlementStatus.APPROVED);
        } else {
            settlement.setStatus(SettlementStatus.REJECTED);
            settlement.getAdvance().setStatus(AdvanceStatus.DISBURSED); // Revert so employee can adjust & resubmit
            advanceRepository.save(settlement.getAdvance());
        }

        settlement.setManagerComment(reviewDto.getComment());
        settlement.setReviewedBy(reviewer);
        settlement.setReviewedAt(LocalDateTime.now());

        return mapToDto(settlementRepository.save(settlement));
    }

    /**
     * Marks a settlement as COMPLETED only after the required refund or payment has been verified.
     */
    @Transactional
    public SettlementResponseDto completeSettlement(Long settlementId, String paymentOrRefundReference) {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers can complete and finalize settlements.");
        }

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with ID: " + settlementId));

        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setPaymentOrRefundReference(paymentOrRefundReference != null ? paymentOrRefundReference : "VERIFIED_CASH_OR_BANK");
        settlement.setCompletedDate(LocalDate.now());

        // Close out the advance request
        AdvanceRequest advance = settlement.getAdvance();
        advance.setStatus(AdvanceStatus.SETTLED);
        advanceRepository.save(advance);

        return mapToDto(settlementRepository.save(settlement));
    }

    public SettlementResponseDto mapToDto(Settlement s) {
        SettlementResponseDto dto = new SettlementResponseDto();
        dto.setId(s.getId());
        dto.setAdvanceId(s.getAdvance().getId());
        dto.setAdvancePurpose(s.getAdvance().getPurpose());
        dto.setUserId(s.getUser().getId());
        dto.setEmployeeName(s.getUser().getFullName());
        dto.setEmployeeEmail(s.getUser().getEmail());
        dto.setTotalDisbursedAdvance(s.getTotalDisbursedAdvance());
        dto.setTotalEligibleExpenses(s.getTotalEligibleExpenses());
        dto.setRemainingBalance(s.getRemainingBalance());
        dto.setOutcome(s.getOutcome());
        dto.setStatus(s.getStatus());
        dto.setSubmissionNotes(s.getSubmissionNotes());
        dto.setManagerComment(s.getManagerComment());
        dto.setPaymentOrRefundReference(s.getPaymentOrRefundReference());
        dto.setCompletedDate(s.getCompletedDate());
        if (s.getReviewedBy() != null) {
            dto.setReviewedByName(s.getReviewedBy().getFullName());
        }
        dto.setReviewedAt(s.getReviewedAt());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
