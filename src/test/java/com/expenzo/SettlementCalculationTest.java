package com.expenzo;

import com.expenzo.dto.ReconciliationSubmitDto;
import com.expenzo.dto.SettlementResponseDto;
import com.expenzo.entity.AdvanceRequest;
import com.expenzo.entity.Expense;
import com.expenzo.entity.Settlement;
import com.expenzo.entity.User;
import com.expenzo.enums.*;
import com.expenzo.repository.AdvanceRequestRepository;
import com.expenzo.repository.ExpenseRepository;
import com.expenzo.repository.SettlementRepository;
import com.expenzo.security.SecurityUtils;
import com.expenzo.service.SettlementService;
import com.expenzo.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettlementCalculationTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private AdvanceRequestRepository advanceRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SettlementService settlementService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private User testUser;
    private AdvanceRequest testAdvance;

    @BeforeEach
    void setUp() {
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserEmail).thenReturn("sarah@company.com");

        testUser = new User("Sarah Jenkins", "sarah@company.com", "pass", Role.ROLE_EMPLOYEE, "Dev");
        testUser.setId(10L);

        testAdvance = new AdvanceRequest();
        testAdvance.setId(101L);
        testAdvance.setUser(testUser);
        testAdvance.setPurpose("Tech Conference in Mumbai");
        testAdvance.setRequestedAmount(new BigDecimal("20000.00"));
        testAdvance.setDisbursedAmount(new BigDecimal("20000.00"));
        testAdvance.setStatus(AdvanceStatus.DISBURSED);

        when(userService.getUserByEmail("sarah@company.com")).thenReturn(testUser);
        when(advanceRepository.findById(101L)).thenReturn(Optional.of(testAdvance));
        when(settlementRepository.findByAdvanceId(101L)).thenReturn(Optional.empty());
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("Example A: Advance ₹20,000, Approved Spends ₹15,000 -> RETURN_DUE ₹5,000")
    void testReturnDueCalculation() {
        Expense exp1 = new Expense();
        exp1.setAmount(new BigDecimal("15000.00"));
        exp1.setStatus(ExpenseStatus.APPROVED);

        when(expenseRepository.findByAdvanceIdOrderByExpenseDateDesc(101L))
                .thenReturn(Arrays.asList(exp1));

        ReconciliationSubmitDto dto = new ReconciliationSubmitDto();
        dto.setAdvanceId(101L);
        dto.setSubmissionNotes("Mumbai event completed under budget");

        SettlementResponseDto result = settlementService.submitReconciliation(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("20000.00"), result.getTotalDisbursedAdvance());
        assertEquals(new BigDecimal("15000.00"), result.getTotalEligibleExpenses());
        // 20000 - 15000 = 5000 (Employee must refund ₹5,000 to company)
        assertEquals(new BigDecimal("5000.00"), result.getRemainingBalance());
        assertEquals(ReconciliationOutcome.RETURN_DUE, result.getOutcome());
    }

    @Test
    @DisplayName("Example B: Advance ₹20,000, Approved Spends ₹23,000 -> ADDITIONAL_PAYMENT_DUE ₹3,000")
    void testAdditionalPaymentDueCalculation() {
        Expense exp1 = new Expense();
        exp1.setAmount(new BigDecimal("23000.00"));
        exp1.setStatus(ExpenseStatus.APPROVED);

        when(expenseRepository.findByAdvanceIdOrderByExpenseDateDesc(101L))
                .thenReturn(Arrays.asList(exp1));

        ReconciliationSubmitDto dto = new ReconciliationSubmitDto();
        dto.setAdvanceId(101L);

        SettlementResponseDto result = settlementService.submitReconciliation(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("20000.00"), result.getTotalDisbursedAdvance());
        assertEquals(new BigDecimal("23000.00"), result.getTotalEligibleExpenses());
        // 20000 - 23000 = -3000 (Company owes employee additional ₹3,000)
        assertEquals(new BigDecimal("-3000.00"), result.getRemainingBalance());
        assertEquals(ReconciliationOutcome.ADDITIONAL_PAYMENT_DUE, result.getOutcome());
    }

    @Test
    @DisplayName("Example C: Advance ₹20,000, Approved Spends ₹20,000 -> BALANCED (Zero difference)")
    void testBalancedCalculation() {
        Expense exp1 = new Expense();
        exp1.setAmount(new BigDecimal("20000.00"));
        exp1.setStatus(ExpenseStatus.APPROVED);

        when(expenseRepository.findByAdvanceIdOrderByExpenseDateDesc(101L))
                .thenReturn(Arrays.asList(exp1));

        ReconciliationSubmitDto dto = new ReconciliationSubmitDto();
        dto.setAdvanceId(101L);

        SettlementResponseDto result = settlementService.submitReconciliation(dto);

        assertNotNull(result);
        assertEquals(0, result.getRemainingBalance().compareTo(BigDecimal.ZERO));
        assertEquals(ReconciliationOutcome.BALANCED, result.getOutcome());
    }

    @Test
    @DisplayName("Policy Rule: Pending and Rejected expenses MUST be excluded from approved eligible total")
    void testExclusionOfPendingAndRejectedExpenses() {
        Expense approvedExp = new Expense();
        approvedExp.setAmount(new BigDecimal("12000.00"));
        approvedExp.setStatus(ExpenseStatus.APPROVED);

        Expense pendingExp = new Expense();
        pendingExp.setAmount(new BigDecimal("4000.00"));
        pendingExp.setStatus(ExpenseStatus.PENDING); // Should NOT count!

        Expense rejectedExp = new Expense();
        rejectedExp.setAmount(new BigDecimal("3000.00"));
        rejectedExp.setStatus(ExpenseStatus.REJECTED); // Should NOT count!

        when(expenseRepository.findByAdvanceIdOrderByExpenseDateDesc(101L))
                .thenReturn(Arrays.asList(approvedExp, pendingExp, rejectedExp));

        ReconciliationSubmitDto dto = new ReconciliationSubmitDto();
        dto.setAdvanceId(101L);

        SettlementResponseDto result = settlementService.submitReconciliation(dto);

        // Only ₹12,000 should be counted as eligible
        assertEquals(new BigDecimal("12000.00"), result.getTotalEligibleExpenses());
        // Remaining balance: 20000 - 12000 = 8000
        assertEquals(new BigDecimal("8000.00"), result.getRemainingBalance());
        assertEquals(ReconciliationOutcome.RETURN_DUE, result.getOutcome());
    }
}
