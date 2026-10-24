package com.expenzo;

import com.expenzo.dto.DisbursementDto;
import com.expenzo.dto.ReviewRequestDto;
import com.expenzo.entity.AdvanceRequest;
import com.expenzo.entity.User;
import com.expenzo.enums.AdvanceStatus;
import com.expenzo.enums.Role;
import com.expenzo.exception.UnauthorizedException;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityOwnershipTest {

    @Mock
    private AdvanceRequestRepository advanceRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdvanceService advanceService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private User employeeA;
    private User employeeB;

    @BeforeEach
    void setUp() {
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);

        employeeA = new User("Sarah Jenkins", "sarah@company.com", "pass", Role.ROLE_EMPLOYEE, "Dev");
        employeeA.setId(10L);

        employeeB = new User("Alex Rivera", "alex@company.com", "pass", Role.ROLE_EMPLOYEE, "DevOps");
        employeeB.setId(20L);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("Ownership Guard: Employee cannot view another employee's advance record")
    void testEmployeeCannotViewAnotherEmployeesRecord() {
        // Logged in as Employee B
        securityUtilsMock.when(SecurityUtils::getCurrentUserEmail).thenReturn("alex@company.com");
        securityUtilsMock.when(SecurityUtils::isManagerOrAdmin).thenReturn(false);

        // Record belongs to Employee A
        AdvanceRequest recordA = new AdvanceRequest();
        recordA.setId(101L);
        recordA.setUser(employeeA);

        when(advanceRepository.findById(101L)).thenReturn(Optional.of(recordA));

        assertThrows(UnauthorizedException.class, () -> advanceService.getAdvanceById(101L));
    }

    @Test
    @DisplayName("Role Guard: Regular employee cannot approve advance requests")
    void testEmployeeCannotApproveAdvance() {
        // Logged in as normal employee
        securityUtilsMock.when(SecurityUtils::isManagerOrAdmin).thenReturn(false);

        ReviewRequestDto review = new ReviewRequestDto("APPROVE", "Self-approving");
        assertThrows(UnauthorizedException.class, () -> advanceService.reviewAdvance(101L, review));
    }

    @Test
    @DisplayName("Role Guard: Regular employee cannot record cash disbursement")
    void testEmployeeCannotDisburseAdvance() {
        securityUtilsMock.when(SecurityUtils::isManagerOrAdmin).thenReturn(false);

        DisbursementDto dto = new DisbursementDto();
        dto.setDisbursedAmount(new BigDecimal("10000.00"));
        dto.setDisbursedDate(LocalDate.now());

        assertThrows(UnauthorizedException.class, () -> advanceService.disburseAdvance(101L, dto));
    }
}
