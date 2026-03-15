package com.dilem.framebackend.model.dto.entry;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateEntryRequest(
    @Size(max = 255) String title,
    String freeText,
    @Size(max = 32) String mood,
    String valuesJson,
    @Size(max = 512) String locationJson,
    @Size(max = 256) String weatherJson,
    Integer templateId,
    LocalDateTime createdAt,
    LocalDateTime capsuleUnlockAt,
    Boolean isCapsuleSealed
) {}
