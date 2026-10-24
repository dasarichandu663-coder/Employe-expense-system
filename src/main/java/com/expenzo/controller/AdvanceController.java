package com.expenzo.controller;

import com.expenzo.dto.AdvanceRequestDto;
import com.expenzo.dto.AdvanceResponseDto;
import com.expenzo.dto.DisbursementDto;
import com.expenzo.dto.ReviewRequestDto;
import com.expenzo.enums.AdvanceStatus;
import com.expenzo.service.AdvanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advances")
public class AdvanceController {

    private final AdvanceService advanceService;

    public AdvanceController(AdvanceService advanceService) {
        this.advanceService = advanceService;
    }

    @PostMapping
    public ResponseEntity<AdvanceResponseDto> createAdvance(@Valid @RequestBody AdvanceRequestDto dto) {
        AdvanceResponseDto created = advanceService.createAdvanceRequest(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<AdvanceResponseDto>> getMyAdvances() {
        return ResponseEntity.ok(advanceService.getMyAdvances());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<AdvanceResponseDto>> getAllAdvances(@RequestParam(required = false) AdvanceStatus status) {
        return ResponseEntity.ok(advanceService.getAllAdvances(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvanceResponseDto> getAdvanceById(@PathVariable Long id) {
        return ResponseEntity.ok(advanceService.getAdvanceById(id));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<AdvanceResponseDto> reviewAdvance(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto reviewDto) {
        return ResponseEntity.ok(advanceService.reviewAdvance(id, reviewDto));
    }

    @PutMapping("/{id}/disburse")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<AdvanceResponseDto> disburseAdvance(
            @PathVariable Long id,
            @Valid @RequestBody DisbursementDto dto) {
        return ResponseEntity.ok(advanceService.disburseAdvance(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvance(@PathVariable Long id) {
        advanceService.deletePendingAdvance(id);
        return ResponseEntity.noContent().build();
    }
}
