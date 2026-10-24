package com.expenzo.controller;

import com.expenzo.dto.ReconciliationSubmitDto;
import com.expenzo.dto.ReviewRequestDto;
import com.expenzo.dto.SettlementResponseDto;
import com.expenzo.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping("/reconcile")
    public ResponseEntity<SettlementResponseDto> submitReconciliation(@Valid @RequestBody ReconciliationSubmitDto dto) {
        SettlementResponseDto result = settlementService.submitReconciliation(dto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<SettlementResponseDto>> getMySettlements() {
        return ResponseEntity.ok(settlementService.getMySettlements());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<SettlementResponseDto>> getAllSettlements() {
        return ResponseEntity.ok(settlementService.getAllSettlements());
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<SettlementResponseDto> reviewSettlement(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto reviewDto) {
        return ResponseEntity.ok(settlementService.reviewSettlement(id, reviewDto));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<SettlementResponseDto> completeSettlement(
            @PathVariable Long id,
            @RequestParam(value = "reference", required = false) String reference) {
        return ResponseEntity.ok(settlementService.completeSettlement(id, reference));
    }
}
