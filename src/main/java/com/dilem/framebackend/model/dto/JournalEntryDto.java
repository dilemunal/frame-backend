package com.dilem.framebackend.model.dto;

import java.time.LocalDateTime;

public record JournalEntryDto(
    Long id,
    Integer templateId,
    String title,
    String freeText,
    String mood,
    String locationJson,
    String weatherJson,
    LocalDateTime createdAt,
    Boolean isCapsuleSealed,
    LocalDateTime capsuleUnlockAt
) {}
