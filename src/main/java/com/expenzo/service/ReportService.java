package com.expenzo.service;

import com.expenzo.entity.AdvanceRequest;
import com.expenzo.entity.ReimbursementClaim;
import com.expenzo.entity.Settlement;
import com.expenzo.exception.UnauthorizedException;
import com.expenzo.repository.AdvanceRequestRepository;
import com.expenzo.repository.ReimbursementClaimRepository;
import com.expenzo.repository.SettlementRepository;
import com.expenzo.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ReportService {

    private final AdvanceRequestRepository advanceRepository;
    private final SettlementRepository settlementRepository;
    private final ReimbursementClaimRepository claimRepository;

    public ReportService(AdvanceRequestRepository advanceRepository,
                         SettlementRepository settlementRepository,
                         ReimbursementClaimRepository claimRepository) {
        this.advanceRepository = advanceRepository;
        this.settlementRepository = settlementRepository;
        this.claimRepository = claimRepository;
    }

    public ByteArrayInputStream generateFinancialAuditCsv() {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers and administrators can export financial reports.");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            // Write CSV Header
            writer.println("RECORD_TYPE,ID,EMPLOYEE_NAME,EMPLOYEE_EMAIL,TITLE_OR_PURPOSE,REQUESTED_OR_CLAIMED_INR,DISBURSED_OR_PAID_INR,ACTUAL_SPENT_INR,RECON_BALANCE_INR,OUTCOME,STATUS,DATE");

            // Advances
            List<AdvanceRequest> advances = advanceRepository.findAllByOrderByCreatedAtDesc();
            for (AdvanceRequest a : advances) {
                writer.printf("ADVANCE,#ADV-%d,\"%s\",\"%s\",\"%s\",%.2f,%s,-,-,-,\"%s\",\"%s\"%n",
                        a.getId(),
                        escape(a.getUser().getFullName()),
                        escape(a.getUser().getEmail()),
                        escape(a.getPurpose()),
                        a.getRequestedAmount(),
                        a.getDisbursedAmount() != null ? String.format("%.2f", a.getDisbursedAmount()) : "-",
                        a.getStatus(),
                        a.getCreatedAt().toLocalDate()
                );
            }

            // Settlements
            List<Settlement> settlements = settlementRepository.findAllByOrderByCreatedAtDesc();
            for (Settlement s : settlements) {
                writer.printf("SETTLEMENT,#STL-%d,\"%s\",\"%s\",\"%s\",-,%.2f,%.2f,%.2f,\"%s\",\"%s\",\"%s\"%n",
                        s.getId(),
                        escape(s.getUser().getFullName()),
                        escape(s.getUser().getEmail()),
                        escape(s.getAdvance().getPurpose()),
                        s.getTotalDisbursedAdvance(),
                        s.getTotalEligibleExpenses(),
                        s.getRemainingBalance(),
                        s.getOutcome(),
                        s.getStatus(),
                        s.getCreatedAt().toLocalDate()
                );
            }

            // Direct Claims
            List<ReimbursementClaim> claims = claimRepository.findAllByOrderByCreatedAtDesc();
            for (ReimbursementClaim c : claims) {
                writer.printf("DIRECT_CLAIM,#CLM-%d,\"%s\",\"%s\",\"%s\",%.2f,%s,-,-,-,\"%s\",\"%s\"%n",
                        c.getId(),
                        escape(c.getUser().getFullName()),
                        escape(c.getUser().getEmail()),
                        escape(c.getTitle()),
                        c.getAmount(),
                        c.getPaidDate() != null ? String.format("%.2f", c.getAmount()) : "-",
                        c.getStatus(),
                        c.getExpenseDate()
                );
            }

            writer.flush();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private String escape(String data) {
        if (data == null) return "";
        return data.replace("\"", "\"\"");
    }
}
