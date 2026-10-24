package com.expenzo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ReviewRequestDto {

    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "(?i)^(APPROVE|REJECT)$", message = "Decision must be APPROVE or REJECT")
    private String decision;

    private String comment;
    private String paymentReference;

    public ReviewRequestDto() {}

    public ReviewRequestDto(String decision, String comment) {
        this.decision = decision;
        this.comment = comment;
    }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
}
