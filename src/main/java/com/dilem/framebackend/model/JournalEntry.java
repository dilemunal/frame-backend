package com.dilem.framebackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "journal_entry")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "template_id")
    private Integer templateId;

    @Column(length = 255)
    private String title;

    @Column(name = "free_text", columnDefinition = "TEXT")
    private String freeText;

    @Column(length = 32)
    private String mood;

    @Column(name = "values_json", columnDefinition = "TEXT")
    private String valuesJson;

    @Column(name = "location_json", length = 512)
    private String locationJson;

    @Column(name = "weather_json", length = 256)
    private String weatherJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Time Capsule fields (for future feature)
    @Column(name = "capsule_unlock_at")
    private LocalDateTime capsuleUnlockAt;

    @Column(name = "is_capsule_sealed", nullable = false)
    @Builder.Default
    private Boolean isCapsuleSealed = false;
}
