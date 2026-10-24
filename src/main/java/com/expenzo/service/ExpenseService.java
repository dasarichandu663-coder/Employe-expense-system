package com.expenzo.service;

import com.expenzo.dto.ExpenseDto;
import com.expenzo.dto.ExpenseResponseDto;
import com.expenzo.dto.ReviewRequestDto;
import com.expenzo.entity.AdvanceRequest;
import com.expenzo.entity.Expense;
import com.expenzo.entity.User;
import com.expenzo.enums.AdvanceStatus;
import com.expenzo.enums.ExpenseStatus;
import com.expenzo.exception.BadRequestException;
import com.expenzo.exception.ResourceNotFoundException;
import com.expenzo.exception.UnauthorizedException;
import com.expenzo.repository.AdvanceRequestRepository;
import com.expenzo.repository.ExpenseRepository;
import com.expenzo.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AdvanceRequestRepository advanceRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public ExpenseService(ExpenseRepository expenseRepository,
                          AdvanceRequestRepository advanceRepository,
                          UserService userService,
                          FileStorageService fileStorageService) {
        this.expenseRepository = expenseRepository;
        this.advanceRepository = advanceRepository;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ExpenseResponseDto createExpense(ExpenseDto dto, MultipartFile receiptFile) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        User user = userService.getUserByEmail(currentUserEmail);

        AdvanceRequest advance = advanceRepository.findById(dto.getAdvanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Advance request not found with ID: " + dto.getAdvanceId()));

        if (!advance.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only log expenses against your own advances.");
        }

        if (advance.getStatus() != AdvanceStatus.DISBURSED && advance.getStatus() != AdvanceStatus.APPROVED) {
            throw new BadRequestException("Expenses can only be logged against active disbursed advances. Current status: " + advance.getStatus());
        }

        Expense expense = new Expense();
        expense.setAdvance(advance);
        expense.setUser(user);
        expense.setTitle(dto.getTitle().trim());
        expense.setCategory(dto.getCategory());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setAmount(dto.getAmount());
        expense.setMerchant(dto.getMerchant());
        expense.setDescription(dto.getDescription());
        expense.setStatus(ExpenseStatus.PENDING);

        if (receiptFile != null && !receiptFile.isEmpty()) {
            String storedFileName = fileStorageService.storeFile(receiptFile);
            expense.setReceiptFilename(storedFileName);
            expense.setReceiptContentType(receiptFile.getContentType());
        }

        Expense saved = expenseRepository.save(expense);
        return mapToDto(saved);
    }

    public List<ExpenseResponseDto> getExpensesByAdvance(Long advanceId) {
        AdvanceRequest advance = advanceRepository.findById(advanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Advance request not found with ID: " + advanceId));

        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        if (!SecurityUtils.isManagerOrAdmin() && !advance.getUser().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedException("You are not authorized to view expenses for this advance.");
        }

        return expenseRepository.findByAdvanceIdOrderByExpenseDateDesc(advanceId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public ExpenseResponseDto getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        if (!SecurityUtils.isManagerOrAdmin() && !expense.getUser().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedException("You are not authorized to view this expense.");
        }

        return mapToDto(expense);
    }

    @Transactional
    public ExpenseResponseDto reviewExpense(Long id, ReviewRequestDto reviewDto) {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers can review expenses.");
        }

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

        User reviewer = userService.getUserByEmail(SecurityUtils.getCurrentUserEmail());

        if ("APPROVE".equalsIgnoreCase(reviewDto.getDecision())) {
            expense.setStatus(ExpenseStatus.APPROVED);
        } else {
            expense.setStatus(ExpenseStatus.REJECTED);
        }

        expense.setManagerComment(reviewDto.getComment());
        expense.setReviewedBy(reviewer);
        expense.setReviewedAt(LocalDateTime.now());

        return mapToDto(expenseRepository.save(expense));
    }

    @Transactional
    public void deletePendingExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        if (!expense.getUser().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedException("You can only delete your own expenses.");
        }

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Only PENDING expenses can be cancelled or deleted.");
        }

        expenseRepository.delete(expense);
    }

    public ExpenseResponseDto mapToDto(Expense e) {
        ExpenseResponseDto dto = new ExpenseResponseDto();
        dto.setId(e.getId());
        dto.setAdvanceId(e.getAdvance().getId());
        dto.setUserId(e.getUser().getId());
        dto.setEmployeeName(e.getUser().getFullName());
        dto.setEmployeeEmail(e.getUser().getEmail());
        dto.setTitle(e.getTitle());
        dto.setCategory(e.getCategory());
        dto.setExpenseDate(e.getExpenseDate());
        dto.setAmount(e.getAmount());
        dto.setMerchant(e.getMerchant());
        dto.setDescription(e.getDescription());
        dto.setReceiptFilename(e.getReceiptFilename());
        dto.setHasReceipt(e.getReceiptFilename() != null && !e.getReceiptFilename().isBlank());
        dto.setStatus(e.getStatus());
        dto.setManagerComment(e.getManagerComment());
        if (e.getReviewedBy() != null) {
            dto.setReviewedByName(e.getReviewedBy().getFullName());
        }
        dto.setReviewedAt(e.getReviewedAt());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
