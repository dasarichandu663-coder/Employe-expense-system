package com.expenzo.controller;

import com.expenzo.dto.ReimbursementClaimDto;
import com.expenzo.dto.ReimbursementResponseDto;
import com.expenzo.dto.ReviewRequestDto;
import com.expenzo.enums.ClaimStatus;
import com.expenzo.enums.ExpenseCategory;
import com.expenzo.service.ReimbursementService;
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
@RequestMapping("/api/claims")
public class ReimbursementClaimController {

    private final ReimbursementService reimbursementService;

    public ReimbursementClaimController(ReimbursementService reimbursementService) {
        this.reimbursementService = reimbursementService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReimbursementResponseDto> submitClaimWithReceipt(
            @RequestParam("title") String title,
            @RequestParam("category") ExpenseCategory category,
            @RequestParam("expenseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expenseDate,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "merchant", required = false) String merchant,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "receipt", required = false) MultipartFile receipt) {

        ReimbursementClaimDto dto = new ReimbursementClaimDto();
        dto.setTitle(title);
        dto.setCategory(category);
        dto.setExpenseDate(expenseDate);
        dto.setAmount(amount);
        dto.setMerchant(merchant);
        dto.setDescription(description);

        ReimbursementResponseDto created = reimbursementService.submitClaim(dto, receipt);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReimbursementResponseDto> submitClaimJson(@Valid @RequestBody ReimbursementClaimDto dto) {
        ReimbursementResponseDto created = reimbursementService.submitClaim(dto, null);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReimbursementResponseDto>> getMyClaims() {
        return ResponseEntity.ok(reimbursementService.getMyClaims());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<ReimbursementResponseDto>> getAllClaims(@RequestParam(required = false) ClaimStatus status) {
        return ResponseEntity.ok(reimbursementService.getAllClaims(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReimbursementResponseDto> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(reimbursementService.getClaimById(id));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ReimbursementResponseDto> reviewClaim(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto reviewDto) {
        return ResponseEntity.ok(reimbursementService.reviewClaim(id, reviewDto));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ReimbursementResponseDto> recordPayment(
            @PathVariable Long id,
            @RequestParam(value = "reference", required = false) String reference) {
        return ResponseEntity.ok(reimbursementService.recordClaimPayment(id, reference));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClaim(@PathVariable Long id) {
        reimbursementService.deletePendingClaim(id);
        return ResponseEntity.noContent().build();
    }
}
