package com.expenzo.service;

import com.expenzo.dto.DashboardSummaryDto;
import com.expenzo.entity.User;
import com.expenzo.enums.AdvanceStatus;
import com.expenzo.enums.ClaimStatus;
import com.expenzo.enums.SettlementStatus;
import com.expenzo.repository.AdvanceRequestRepository;
import com.expenzo.repository.ReimbursementClaimRepository;
import com.expenzo.repository.SettlementRepository;
import com.expenzo.repository.UserRepository;
import com.expenzo.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {

    private final AdvanceRequestRepository advanceRepository;
    private final ReimbursementClaimRepository claimRepository;
    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public DashboardService(AdvanceRequestRepository advanceRepository,
                            ReimbursementClaimRepository claimRepository,
                            SettlementRepository settlementRepository,
                            UserRepository userRepository,
                            UserService userService) {
        this.advanceRepository = advanceRepository;
        this.claimRepository = claimRepository;
        this.settlementRepository = settlementRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public DashboardSummaryDto getDashboardSummary() {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        User currentUser = userService.getUserByEmail(currentUserEmail);
        boolean isManager = SecurityUtils.isManagerOrAdmin();

        DashboardSummaryDto dto = new DashboardSummaryDto();

        if (isManager) {
            // Manager / Admin Company-wide metrics
            dto.setPendingAdvanceCount(advanceRepository.countByStatus(AdvanceStatus.PENDING));
            dto.setPendingSettlementCount(settlementRepository.countByStatus(SettlementStatus.PENDING_REVIEW));
            dto.setTotalDisbursedAmount(advanceRepository.sumTotalDisbursedAll());
            dto.setRegisteredEmployeeCount(userRepository.count());

            dto.setApprovedReimbursementAmount(claimRepository.sumApprovedClaimsAll());
            dto.setDirectClaimCount(claimRepository.count());
            dto.setTotalAdvanceCount(advanceRepository.count());
            dto.setActiveAdvanceAmount(dto.getTotalDisbursedAmount());
        } else {
            // Employee personal metrics
            BigDecimal activeAdvance = advanceRepository.sumActiveDisbursedByUser(currentUser);
            dto.setActiveAdvanceAmount(activeAdvance != null ? activeAdvance : BigDecimal.ZERO);

            long pendingSettlements = settlementRepository.countByUserAndStatus(currentUser, SettlementStatus.PENDING_REVIEW);
            dto.setPendingSettlementCount(pendingSettlements);

            BigDecimal approvedClaims = claimRepository.sumApprovedClaimsByUser(currentUser);
            dto.setApprovedReimbursementAmount(approvedClaims != null ? approvedClaims : BigDecimal.ZERO);

            long claimsCount = claimRepository.countByUser(currentUser);
            dto.setDirectClaimCount(claimsCount);
        }

        return dto;
    }
}
