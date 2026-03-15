package com.dilem.framebackend.service;

import com.dilem.framebackend.model.JournalEntry;
import com.dilem.framebackend.model.User;
import com.dilem.framebackend.model.dto.entry.CreateEntryRequest;
import com.dilem.framebackend.model.dto.entry.EntryResponse;
import com.dilem.framebackend.model.dto.entry.UpdateEntryRequest;
import com.dilem.framebackend.repository.JournalEntryRepository;
import com.dilem.framebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EntryService {

    private final JournalEntryRepository entryRepository;
    private final UserRepository userRepository;

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public Page<EntryResponse> getEntries(String email, int page, int size) {
        User user = resolveUser(email);
        return entryRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size))
                .map(EntryResponse::fromEntity);
    }

    public EntryResponse getEntry(String email, Long id) {
        User user = resolveUser(email);
        JournalEntry entry = entryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
        return EntryResponse.fromEntity(entry);
    }

    public EntryResponse createEntry(String email, CreateEntryRequest req) {
        User user = resolveUser(email);

        JournalEntry entry = JournalEntry.builder()
                .user(user)
                .templateId(req.templateId())
                .title(req.title())
                .freeText(req.freeText())
                .mood(req.mood())
                .valuesJson(req.valuesJson())
                .locationJson(req.locationJson())
                .weatherJson(req.weatherJson())
                .createdAt(req.createdAt() != null ? req.createdAt() : LocalDateTime.now())
                .isCapsuleSealed(req.isCapsuleSealed() != null && req.isCapsuleSealed())
                .capsuleUnlockAt(req.capsuleUnlockAt())
                .build();

        JournalEntry saved = entryRepository.save(entry);
        return EntryResponse.fromEntity(saved);
    }

    public EntryResponse updateEntry(String email, Long id, UpdateEntryRequest req) {
        User user = resolveUser(email);
        JournalEntry entry = entryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        if (req.templateId() != null) entry.setTemplateId(req.templateId());
        if (req.title() != null) entry.setTitle(req.title());
        if (req.freeText() != null) entry.setFreeText(req.freeText());
        if (req.mood() != null) entry.setMood(req.mood());
        if (req.valuesJson() != null) entry.setValuesJson(req.valuesJson());
        if (req.locationJson() != null) entry.setLocationJson(req.locationJson());
        if (req.weatherJson() != null) entry.setWeatherJson(req.weatherJson());
        if (req.createdAt() != null) entry.setCreatedAt(req.createdAt());
        if (req.isCapsuleSealed() != null) entry.setIsCapsuleSealed(req.isCapsuleSealed());
        if (req.capsuleUnlockAt() != null) entry.setCapsuleUnlockAt(req.capsuleUnlockAt());

        JournalEntry updated = entryRepository.save(entry);
        return EntryResponse.fromEntity(updated);
    }

    public void deleteEntry(String email, Long id) {
        User user = resolveUser(email);
        JournalEntry entry = entryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
        entryRepository.deleteByIdAndUserId(id, user.getId());
    }
}
