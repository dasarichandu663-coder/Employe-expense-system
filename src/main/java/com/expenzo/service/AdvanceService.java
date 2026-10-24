package com.expenzo.service;

import com.expenzo.dto.AdvanceRequestDto;
import com.expenzo.dto.AdvanceResponseDto;
import com.expenzo.dto.DisbursementDto;
import com.expenzo.dto.ReviewRequestDto;
import com.expenzo.entity.AdvanceRequest;
import com.expenzo.entity.User;
import com.expenzo.enums.AdvanceStatus;
import com.expenzo.exception.BadRequestException;
import com.expenzo.exception.ResourceNotFoundException;
import com.expenzo.exception.UnauthorizedException;
import com.expenzo.repository.AdvanceRequestRepository;
import com.expenzo.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdvanceService {

    private final AdvanceRequestRepository advanceRepository;
    private final UserService userService;

    public AdvanceService(AdvanceRequestRepository advanceRepository, UserService userService) {
        this.advanceRepository = advanceRepository;
        this.userService = userService;
    }

    @Transactional
    public AdvanceResponseDto createAdvanceRequest(AdvanceRequestDto dto) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        User user = userService.getUserByEmail(currentUserEmail);

        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date.");
        }

        AdvanceRequest advance = new AdvanceRequest();
        advance.setUser(user);
        advance.setPurpose(dto.getPurpose().trim());
        advance.setDestination(dto.getDestination());
        advance.setStartDate(dto.getStartDate());
        advance.setEndDate(dto.getEndDate());
        advance.setRequestedAmount(dto.getRequestedAmount());
        advance.setNotes(dto.getNotes());
        advance.setStatus(AdvanceStatus.PENDING);

        AdvanceRequest saved = advanceRepository.save(advance);
        return mapToDto(saved);
    }

    public List<AdvanceResponseDto> getMyAdvances() {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        User user = userService.getUserByEmail(currentUserEmail);
        return advanceRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<AdvanceResponseDto> getAllAdvances(AdvanceStatus filterStatus) {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers and administrators can view all staff advances.");
        }

        List<AdvanceRequest> list;
        if (filterStatus != null) {
            list = advanceRepository.findByStatusOrderByCreatedAtDesc(filterStatus);
        } else {
            list = advanceRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public AdvanceResponseDto getAdvanceById(Long id) {
        AdvanceRequest advance = advanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance request not found with ID: " + id));

        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        if (!SecurityUtils.isManagerOrAdmin() && !advance.getUser().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedException("You are not authorized to view this advance record.");
        }

        return mapToDto(advance);
    }

    @Transactional
    public AdvanceResponseDto reviewAdvance(Long id, ReviewRequestDto reviewDto) {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers can review advance requests.");
        }

        AdvanceRequest advance = advanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance request not found with ID: " + id));

        if (advance.getStatus() != AdvanceStatus.PENDING) {
            throw new BadRequestException("Advance request #" + id + " has already been reviewed (" + advance.getStatus() + ").");
        }

        User reviewer = userService.getUserByEmail(SecurityUtils.getCurrentUserEmail());

        if ("APPROVE".equalsIgnoreCase(reviewDto.getDecision())) {
            advance.setStatus(AdvanceStatus.APPROVED);
        } else {
            advance.setStatus(AdvanceStatus.REJECTED);
        }

        advance.setManagerComment(reviewDto.getComment());
        advance.setReviewedBy(reviewer);
        advance.setReviewedAt(LocalDateTime.now());

        return mapToDto(advanceRepository.save(advance));
    }

    @Transactional
    public AdvanceResponseDto disburseAdvance(Long id, DisbursementDto dto) {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new UnauthorizedException("Only managers can record advance disbursements.");
        }

        AdvanceRequest advance = advanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance request not found with ID: " + id));

        if (advance.getStatus() != AdvanceStatus.APPROVED) {
            throw new BadRequestException("Only approved advance requests can be disbursed. Current status: " + advance.getStatus());
        }

        advance.setDisbursedAmount(dto.getDisbursedAmount());
        advance.setDisbursedDate(dto.getDisbursedDate());
        if (dto.getManagerComment() != null && !dto.getManagerComment().isBlank()) {
            advance.setManagerComment(dto.getManagerComment());
        }
        advance.setStatus(AdvanceStatus.DISBURSED);

        return mapToDto(advanceRepository.save(advance));
    }

    @Transactional
    public void deletePendingAdvance(Long id) {
        AdvanceRequest advance = advanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance request not found with ID: " + id));

        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        if (!advance.getUser().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new UnauthorizedException("You can only cancel your own advance requests.");
        }

        if (advance.getStatus() != AdvanceStatus.PENDING) {
            throw new BadRequestException("Only PENDING advance requests can be cancelled. Current status is " + advance.getStatus());
        }

        advanceRepository.delete(advance);
    }

    public AdvanceResponseDto mapToDto(AdvanceRequest a) {
        AdvanceResponseDto dto = new AdvanceResponseDto();
        dto.setId(a.getId());
        dto.setUserId(a.getUser().getId());
        dto.setEmployeeName(a.getUser().getFullName());
        dto.setEmployeeEmail(a.getUser().getEmail());
        dto.setPurpose(a.getPurpose());
        dto.setDestination(a.getDestination());
        dto.setStartDate(a.getStartDate());
        dto.setEndDate(a.getEndDate());
        dto.setRequestedAmount(a.getRequestedAmount());
        dto.setDisbursedAmount(a.getDisbursedAmount());
        dto.setDisbursedDate(a.getDisbursedDate());
        dto.setNotes(a.getNotes());
        dto.setStatus(a.getStatus());
        dto.setManagerComment(a.getManagerComment());
        if (a.getReviewedBy() != null) {
            dto.setReviewedByName(a.getReviewedBy().getFullName());
        }
        dto.setReviewedAt(a.getReviewedAt());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
