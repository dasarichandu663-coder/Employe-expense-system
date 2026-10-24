package com.expenzo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class DisbursementDto {

    @NotNull(message = "Disbursed amount is required")
    @DecimalMin(value = "0.01", message = "Disbursed amount must be greater than zero")
    private BigDecimal disbursedAmount;

    @NotNull(message = "Disbursement date is required")
    private LocalDate disbursedDate;

    private String managerComment;

    public DisbursementDto() {}

    public BigDecimal getDisbursedAmount() { return disbursedAmount; }
    public void setDisbursedAmount(BigDecimal disbursedAmount) { this.disbursedAmount = disbursedAmount; }

    public LocalDate getDisbursedDate() { return disbursedDate; }
    public void setDisbursedDate(LocalDate disbursedDate) { this.disbursedDate = disbursedDate; }

    public String getManagerComment() { return managerComment; }
    public void setManagerComment(String managerComment) { this.managerComment = managerComment; }
}
