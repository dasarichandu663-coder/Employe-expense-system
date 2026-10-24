package com.expenzo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AdvanceRequestDto {

    @NotBlank(message = "Purpose is required")
    private String purpose;

    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "1.00", message = "Requested amount must be at least ₹1.00")
    private BigDecimal requestedAmount;

    private String notes;

    public AdvanceRequestDto() {}

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
