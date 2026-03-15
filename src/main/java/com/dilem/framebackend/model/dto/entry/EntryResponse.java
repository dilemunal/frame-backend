package com.dilem.framebackend.model.dto.entry;

import com.dilem.framebackend.model.JournalEntry;

import java.time.LocalDateTime;

public record EntryResponse(
    Long id,
    Integer templateId,
    String title,
    String freeText,
    String mood,
    String valuesJson,
    String locationJson,
    String weatherJson,
    LocalDateTime createdAt,
    Boolean isCapsuleSealed,
    LocalDateTime capsuleUnlockAt
) {
    public static EntryResponse fromEntity(JournalEntry e) {
        if (e.getIsCapsuleSealed() != null && e.getIsCapsuleSealed() && e.getCapsuleUnlockAt() != null && e.getCapsuleUnlockAt().isAfter(LocalDateTime.now())) {
            return new EntryResponse(
                e.getId(),
                e.getTemplateId(),
                null, // Hidden
                null, // Hidden
                e.getMood(),
                null, // Hidden
                e.getLocationJson(),
                e.getWeatherJson(),
                e.getCreatedAt(),
                e.getIsCapsuleSealed(),
                e.getCapsuleUnlockAt()
            );
        }

        return new EntryResponse(
            e.getId(),
            e.getTemplateId(),
            e.getTitle(),
            e.getFreeText(),
            e.getMood(),
            e.getValuesJson(),
            e.getLocationJson(),
            e.getWeatherJson(),
            e.getCreatedAt(),
            e.getIsCapsuleSealed(),
            e.getCapsuleUnlockAt()
        );
    }
}
