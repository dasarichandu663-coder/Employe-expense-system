package com.expenzo.config;

import com.expenzo.entity.*;
import com.expenzo.enums.*;
import com.expenzo.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdvanceRequestRepository advanceRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final ReimbursementClaimRepository claimRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${expenzo.init.admin-email:admin@expenzo.com}")
    private String adminEmail;

    @Value("${expenzo.init.admin-password:Admin@1234}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository,
                           AdvanceRequestRepository advanceRepository,
                           ExpenseRepository expenseRepository,
                           SettlementRepository settlementRepository,
                           ReimbursementClaimRepository claimRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.advanceRepository = advanceRepository;
        this.expenseRepository = expenseRepository;
        this.settlementRepository = settlementRepository;
        this.claimRepository = claimRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Provision Secure Development Administrator
        User admin = userRepository.findByEmail(adminEmail.toLowerCase().trim()).orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setFullName("Super Administrator");
            admin.setEmail(adminEmail.toLowerCase().trim());
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ROLE_ADMIN);
            admin.setDepartment("Executive Leadership");
            admin.setActive(true);
            admin = userRepository.save(admin);
            System.out.println(">>> [EXPENZO] Initial Administrator provisioned: " + adminEmail);
        }

        // Also ensure legacy "admin" or "admin@company.com" alias works for quick student testing
        if (!userRepository.existsByEmail("admin@company.com")) {
            User adminAlias = new User();
            adminAlias.setFullName("Company Finance Manager");
            adminAlias.setEmail("admin@company.com");
            adminAlias.setPassword(passwordEncoder.encode("admin123"));
            adminAlias.setRole(Role.ROLE_MANAGER);
            adminAlias.setDepartment("Finance & Accounts");
            adminAlias.setActive(true);
            userRepository.save(adminAlias);
        }

        // 2. Provision Demo Employee 1: Sarah Jenkins
        User sarah = userRepository.findByEmail("sarah@company.com").orElse(null);
        if (sarah == null) {
            sarah = new User();
            sarah.setFullName("Sarah Jenkins");
            sarah.setEmail("sarah@company.com");
            sarah.setPassword(passwordEncoder.encode("pass123"));
            sarah.setRole(Role.ROLE_EMPLOYEE);
            sarah.setDepartment("Product Architecture");
            sarah.setActive(true);
            sarah = userRepository.save(sarah);

            // Seed an active disbursed advance for Sarah ready to be settled
            AdvanceRequest adv1 = new AdvanceRequest();
            adv1.setUser(sarah);
            adv1.setPurpose("Bangalore Client Onsite & System Implementation");
            adv1.setDestination("Bangalore, Karnataka");
            adv1.setStartDate(LocalDate.now().minusDays(5));
            adv1.setEndDate(LocalDate.now().minusDays(2));
            adv1.setRequestedAmount(new BigDecimal("25000.00"));
            adv1.setDisbursedAmount(new BigDecimal("25000.00"));
            adv1.setDisbursedDate(LocalDate.now().minusDays(4));
            adv1.setNotes("Flight ₹9,000, 3-star Hotel (3 nights) ₹12,000, Local commute ₹4,000");
            adv1.setStatus(AdvanceStatus.DISBURSED);
            adv1.setReviewedBy(admin);
            adv1.setReviewedAt(LocalDateTime.now().minusDays(4));
            adv1 = advanceRepository.save(adv1);

            // Seed approved expenses against this advance
            Expense exp1 = new Expense();
            exp1.setAdvance(adv1);
            exp1.setUser(sarah);
            exp1.setTitle("Round-Trip Flight Tickets");
            exp1.setCategory(ExpenseCategory.TRAVEL);
            exp1.setExpenseDate(LocalDate.now().minusDays(5));
            exp1.setAmount(new BigDecimal("9200.00"));
            exp1.setMerchant("Indigo Airlines");
            exp1.setDescription("Flight to Kempegowda Intl Airport");
            exp1.setStatus(ExpenseStatus.APPROVED);
            exp1.setReviewedBy(admin);
            exp1.setReviewedAt(LocalDateTime.now().minusDays(3));
            expenseRepository.save(exp1);

            Expense exp2 = new Expense();
            exp2.setAdvance(adv1);
            exp2.setUser(sarah);
            exp2.setTitle("Executive Business Hotel Lodging");
            exp2.setCategory(ExpenseCategory.HOTEL);
            exp2.setExpenseDate(LocalDate.now().minusDays(3));
            exp2.setAmount(new BigDecimal("12500.00"));
            exp2.setMerchant("Marriott Courtyard");
            exp2.setDescription("3 nights stay during client meetings");
            exp2.setStatus(ExpenseStatus.APPROVED);
            exp2.setReviewedBy(admin);
            exp2.setReviewedAt(LocalDateTime.now().minusDays(2));
            expenseRepository.save(exp2);

            // Seed direct reimbursement claim
            ReimbursementClaim claim1 = new ReimbursementClaim();
            claim1.setUser(sarah);
            claim1.setTitle("Cloud IDE Dev Subscription Reimbursement");
            claim1.setCategory(ExpenseCategory.OFFICE_SUPPLIES);
            claim1.setExpenseDate(LocalDate.now().minusDays(2));
            claim1.setAmount(new BigDecimal("2450.00"));
            claim1.setMerchant("JetBrains / GitHub");
            claim1.setDescription("Monthly team subscription paid from personal card.");
            claim1.setStatus(ClaimStatus.APPROVED);
            claim1.setReviewedBy(admin);
            claim1.setReviewedAt(LocalDateTime.now().minusDays(1));
            claimRepository.save(claim1);
        }

        // 3. Provision Demo Employee 2: Alex Rivera
        User alex = userRepository.findByEmail("alex@company.com").orElse(null);
        if (alex == null) {
            alex = new User();
            alex.setFullName("Alex Rivera");
            alex.setEmail("alex@company.com");
            alex.setPassword(passwordEncoder.encode("pass123"));
            alex.setRole(Role.ROLE_EMPLOYEE);
            alex.setDepartment("DevOps & Cloud");
            alex.setActive(true);
            alex = userRepository.save(alex);

            // Seed a pending advance for Alex
            AdvanceRequest adv2 = new AdvanceRequest();
            adv2.setUser(alex);
            adv2.setPurpose("Annual CyberSec Expo in Delhi");
            adv2.setDestination("New Delhi");
            adv2.setStartDate(LocalDate.now().plusDays(2));
            adv2.setEndDate(LocalDate.now().plusDays(4));
            adv2.setRequestedAmount(new BigDecimal("15000.00"));
            adv2.setNotes("Registration pass ₹6,000, Train 2nd AC ₹4,000, Lodging ₹5,000");
            adv2.setStatus(AdvanceStatus.PENDING);
            advanceRepository.save(adv2);

            // Seed direct claim
            ReimbursementClaim claim2 = new ReimbursementClaim();
            claim2.setUser(alex);
            claim2.setTitle("Emergency Server Rack Router & Patch Cable");
            claim2.setCategory(ExpenseCategory.OFFICE_SUPPLIES);
            claim2.setExpenseDate(LocalDate.now());
            claim2.setAmount(new BigDecimal("1850.00"));
            claim2.setMerchant("Local Hardware Supply");
            claim2.setDescription("Replacement cables for office data closet.");
            claim2.setStatus(ClaimStatus.PENDING);
            claimRepository.save(claim2);
        }
    }
}
