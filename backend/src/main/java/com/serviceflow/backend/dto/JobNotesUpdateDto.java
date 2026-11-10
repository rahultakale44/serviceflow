package com.serviceflow.backend.dto;

import jakarta.validation.constraints.Size;

public record JobNotesUpdateDto(
        @Size(max = 1000, message = "Inspection notes cannot exceed 1000 characters")
        String inspectionNotes,

        @Size(max = 1000, message = "Technician notes cannot exceed 1000 characters")
        String technicianNotes
) {
}
