package com.expenzo.controller;

import com.expenzo.dto.ExpenseDto;
import com.expenzo.dto.ExpenseResponseDto;
import com.expenzo.dto.ReviewRequestDto;
import com.expenzo.enums.ExpenseCategory;
import com.expenzo.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExpenseResponseDto> createExpenseWithReceipt(
            @RequestParam("advanceId") Long advanceId,
            @RequestParam("title") String title,
            @RequestParam("category") ExpenseCategory category,
            @RequestParam("expenseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expenseDate,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "merchant", required = false) String merchant,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "receipt", required = false) MultipartFile receipt) {

        ExpenseDto dto = new ExpenseDto();
        dto.setAdvanceId(advanceId);
        dto.setTitle(title);
        dto.setCategory(category);
        dto.setExpenseDate(expenseDate);
        dto.setAmount(amount);
        dto.setMerchant(merchant);
        dto.setDescription(description);

        ExpenseResponseDto created = expenseService.createExpense(dto, receipt);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExpenseResponseDto> createExpenseJson(@Valid @RequestBody ExpenseDto dto) {
        ExpenseResponseDto created = expenseService.createExpense(dto, null);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/advance/{advanceId}")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByAdvance(@PathVariable Long advanceId) {
        return ResponseEntity.ok(expenseService.getExpensesByAdvance(advanceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDto> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ExpenseResponseDto> reviewExpense(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto reviewDto) {
        return ResponseEntity.ok(expenseService.reviewExpense(id, reviewDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deletePendingExpense(id);
        return ResponseEntity.noContent().build();
    }
}
