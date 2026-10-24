package com.expenzo;

import com.expenzo.dto.AdvanceRequestDto;
import com.expenzo.dto.AdvanceResponseDto;
import com.expenzo.dto.DisbursementDto;
import com.expenzo.dto.ReviewRequestDto;
import com.expenzo.entity.AdvanceRequest;
import com.expenzo.entity.User;
import com.expenzo.enums.AdvanceStatus;
import com.expenzo.enums.Role;
import com.expenzo.exception.BadRequestException;
import com.expenzo.repository.AdvanceRequestRepository;
import com.expenzo.security.SecurityUtils;
import com.expenzo.service.AdvanceService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdvanceWorkflowTest {

    @Mock
    private AdvanceRequestRepository advanceRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdvanceService advanceService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private User employee;
    private User manager;

    @BeforeEach
    void setUp() {
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);

        employee = new User("Alex Rivera", "alex@company.com", "pass", Role.ROLE_EMPLOYEE, "DevOps");
        employee.setId(1L);

        manager = new User("Finance Manager", "manager@company.com", "pass", Role.ROLE_MANAGER, "Finance");
        manager.setId(2L);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("Create Advance Request: Initial status must be PENDING")
    void testCreateAdvanceRequestInitialStatus() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserEmail).thenReturn("alex@company.com");
        when(userService.getUserByEmail("alex@company.com")).thenReturn(employee);
        when(advanceRepository.save(any(AdvanceRequest.class))).thenAnswer(inv -> {
            AdvanceRequest a = inv.getArgument(0);
            a.setId(201L);
            return a;
        });

        AdvanceRequestDto dto = new AdvanceRequestDto();
        dto.setPurpose("Cloud Summit Bangalore");
        dto.setRequestedAmount(new BigDecimal("18000.00"));
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(7));

        AdvanceResponseDto result = advanceService.createAdvanceRequest(dto);

        assertNotNull(result);
        assertEquals(AdvanceStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("18000.00"), result.getRequestedAmount());
    }

    @Test
    @DisplayName("Manager Approval: Transitions advance status to APPROVED")
    void testApproveAdvance() {
        securityUtilsMock.when(SecurityUtils::isManagerOrAdmin).thenReturn(true);
        securityUtilsMock.when(SecurityUtils::getCurrentUserEmail).thenReturn("manager@company.com");
        when(userService.getUserByEmail("manager@company.com")).thenReturn(manager);

        AdvanceRequest advance = new AdvanceRequest();
        advance.setId(201L);
        advance.setUser(employee);
        advance.setStatus(AdvanceStatus.PENDING);
        advance.setRequestedAmount(new BigDecimal("18000.00"));

        when(advanceRepository.findById(201L)).thenReturn(Optional.of(advance));
        when(advanceRepository.save(any(AdvanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewRequestDto review = new ReviewRequestDto("APPROVE", "Budget verified and approved.");
        AdvanceResponseDto result = advanceService.reviewAdvance(201L, review);

        assertEquals(AdvanceStatus.APPROVED, result.getStatus());
        assertEquals("Budget verified and approved.", result.getManagerComment());
    }

    @Test
    @DisplayName("Manager Disbursement: Records disbursed amount & date, status becomes DISBURSED")
    void testDisburseAdvance() {
        securityUtilsMock.when(SecurityUtils::isManagerOrAdmin).thenReturn(true);

        AdvanceRequest advance = new AdvanceRequest();
        advance.setId(201L);
        advance.setUser(employee);
        advance.setStatus(AdvanceStatus.APPROVED); // Must be approved first!
        advance.setRequestedAmount(new BigDecimal("18000.00"));

        when(advanceRepository.findById(201L)).thenReturn(Optional.of(advance));
        when(advanceRepository.save(any(AdvanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        DisbursementDto dto = new DisbursementDto();
        dto.setDisbursedAmount(new BigDecimal("18000.00"));
        dto.setDisbursedDate(LocalDate.now());
        dto.setManagerComment("Bank transfer NEFT-839210 processed");

        AdvanceResponseDto result = advanceService.disburseAdvance(201L, dto);

        assertEquals(AdvanceStatus.DISBURSED, result.getStatus());
        assertEquals(new BigDecimal("18000.00"), result.getDisbursedAmount());
        assertEquals(LocalDate.now(), result.getDisbursedDate());
    }

    @Test
    @DisplayName("Disbursement Guard: Cannot disburse a PENDING or REJECTED advance")
    void testCannotDisbursePendingAdvance() {
        securityUtilsMock.when(SecurityUtils::isManagerOrAdmin).thenReturn(true);

        AdvanceRequest advance = new AdvanceRequest();
        advance.setId(201L);
        advance.setStatus(AdvanceStatus.PENDING); // Not yet approved!

        when(advanceRepository.findById(201L)).thenReturn(Optional.of(advance));

        DisbursementDto dto = new DisbursementDto();
        dto.setDisbursedAmount(new BigDecimal("18000.00"));
        dto.setDisbursedDate(LocalDate.now());

        assertThrows(BadRequestException.class, () -> advanceService.disburseAdvance(201L, dto));
    }
}
