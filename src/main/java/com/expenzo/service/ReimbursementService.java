package com.expenzo.service;

import com.expenzo.dto.ReimbursementClaimDto;
import com.expenzo.dto.ReimbursementResponseDto;
import com.expenzo.dto.ReviewRequestDto;
import com.expenzo.entity.ReimbursementClaim;
import com.expenzo.entity.User;
import com.expenzo.enums.ClaimStatus;
import com.expenzo.exception.BadRequestException;
import com.expenzo.exception.ResourceNotFoundException;
import com.expenzo.exception.UnauthorizedException;
import com.expenzo.repository.ReimbursementClaimRepository;
import com.expenzo.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReimbursementService {

    private final ReimbursementClaimRepository claimRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public ReimbursementService(ReimbursementClaimRepository claimRepository,
                                UserService userService,
                                FileStorageService fileStorageService) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ReimbursementResponseDto submitClaim(ReimbursementClaimDto dto, MultipartFile receiptFile) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        User user = userService.getUserByEmail(currentUserEmail);

        ReimbursementClaim claim = new ReimbursementClaim();
        claim.setUser(user);
        claim.setTitle(dto.getTitle().trim());
        claim.setCategory(dto.getCategory());
        claim.setExpenseDate(dto.getExpenseDate());
        claim.setAmount(dto.getAmount());
        claim.setMerchant(dto.getMerchant());
        claim.setDescription(dto.getDescription());
        claim.setStatus(ClaimStatus.PENDING);

        if (receiptFile != null && !receiptFile.isEmpty()) {
            String storedFileName = fileStorageService.storeFile(receiptFile);
            claim.setReceiptFilename(storedFileName);
            claim.setReceiptContentType(receiptFile.getContentType());
        }

        ReimbursementClaim saved = claimRepository.save(claim);
        return mapToDto(saved);
    }

    public List<ReimbursementResponseDto> getMyClaims() {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        User user = userService.getUserByEmail(currentUserEmail);
        return claimRepository.findByUserOrderByExpenseDateDesc(user)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ReimbursementResponseDto> getAllClaims(ClaimStatus filterStatus) {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers can view all staff reimbursement claims.");
        }

        List<ReimbursementClaim> list;
        if (filterStatus != null) {
            list = claimRepository.findByStatusOrderByCreatedAtDesc(filterStatus);
        } else {
            list = claimRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public ReimbursementResponseDto getClaimById(Long id) {
        ReimbursementClaim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));

        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        if (!SecurityUtils.isManagerOrAdmin() && !claim.getUser().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedException("You are not authorized to view this reimbursement claim.");
        }

        return mapToDto(claim);
    }

    @Transactional
    public ReimbursementResponseDto reviewClaim(Long id, ReviewRequestDto reviewDto) {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers can review reimbursement claims.");
        }

        ReimbursementClaim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));

        User reviewer = userService.getUserByEmail(SecurityUtils.getCurrentUserEmail());

        if ("APPROVE".equalsIgnoreCase(reviewDto.getDecision())) {
            claim.setStatus(ClaimStatus.APPROVED);
        } else {
            claim.setStatus(ClaimStatus.REJECTED);
        }

        claim.setManagerComment(reviewDto.getComment());
        claim.setReviewedBy(reviewer);
        claim.setReviewedAt(LocalDateTime.now());

        return mapToDto(claimRepository.save(claim));
    }

    @Transactional
    public ReimbursementResponseDto recordClaimPayment(Long id, String paymentReference) {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers can record payments for reimbursement claims.");
        }

        ReimbursementClaim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("Only approved claims can be marked as paid. Current status: " + claim.getStatus());
        }

        claim.setStatus(ClaimStatus.PAID);
        claim.setPaymentReference(paymentReference != null ? paymentReference : "BANK_DISBURSEMENT");
        claim.setPaidDate(LocalDate.now());

        return mapToDto(claimRepository.save(claim));
    }

    @Transactional
    public void deletePendingClaim(Long id) {
        ReimbursementClaim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));

        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        if (!claim.getUser().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedException("You can only cancel your own claims.");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Only PENDING claims can be deleted.");
        }

        claimRepository.delete(claim);
    }

    public ReimbursementResponseDto mapToDto(ReimbursementClaim c) {
        ReimbursementResponseDto dto = new ReimbursementResponseDto();
        dto.setId(c.getId());
        dto.setUserId(c.getUser().getId());
        dto.setEmployeeName(c.getUser().getFullName());
        dto.setEmployeeEmail(c.getUser().getEmail());
        dto.setTitle(c.getTitle());
        dto.setCategory(c.getCategory());
        dto.setExpenseDate(c.getExpenseDate());
        dto.setAmount(c.getAmount());
        dto.setMerchant(c.getMerchant());
        dto.setDescription(c.getDescription());
        dto.setReceiptFilename(c.getReceiptFilename());
        dto.setHasReceipt(c.getReceiptFilename() != null && !c.getReceiptFilename().isBlank());
        dto.setStatus(c.getStatus());
        dto.setManagerComment(c.getManagerComment());
        dto.setPaymentReference(c.getPaymentReference());
        dto.setPaidDate(c.getPaidDate());
        if (c.getReviewedBy() != null) {
            dto.setReviewedByName(c.getReviewedBy().getFullName());
        }
        dto.setReviewedAt(c.getReviewedAt());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
