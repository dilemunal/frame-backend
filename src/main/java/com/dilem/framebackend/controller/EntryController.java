package com.dilem.framebackend.controller;

import com.dilem.framebackend.model.JournalEntry;
import com.dilem.framebackend.model.dto.JournalEntryDto;
import com.dilem.framebackend.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/entries")
@RequiredArgsConstructor
public class EntryController {

    private final JournalEntryRepository entryRepository;

    @GetMapping
    public ResponseEntity<List<JournalEntryDto>> getMyEntries(Authentication authentication) {
        String email = authentication.getName();
        List<JournalEntry> entries = entryRepository.findByUserEmailOrderByCreatedAtDesc(email);
        
        List<JournalEntryDto> dtos = entries.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(dtos);
    }

    private JournalEntryDto mapToDto(JournalEntry entry) {
        return new JournalEntryDto(
                entry.getId(),
                entry.getTemplateId(),
                entry.getTitle(),
                entry.getFreeText(),
                entry.getMood(),
                entry.getLocationJson(),
                entry.getWeatherJson(),
                entry.getCreatedAt(),
                entry.getIsCapsuleSealed(),
                entry.getCapsuleUnlockAt()
        );
    }
}
