package com.expenzo.dto;

import jakarta.validation.constraints.NotNull;

public class ReconciliationSubmitDto {

    @NotNull(message = "Advance ID is required")
    private Long advanceId;

    private String submissionNotes;

    public ReconciliationSubmitDto() {}

    public Long getAdvanceId() { return advanceId; }
    public void setAdvanceId(Long advanceId) { this.advanceId = advanceId; }

    public String getSubmissionNotes() { return submissionNotes; }
    public void setSubmissionNotes(String submissionNotes) { this.submissionNotes = submissionNotes; }
}
