package com.dilem.framebackend.controller;

import com.dilem.framebackend.model.dto.entry.CreateEntryRequest;
import com.dilem.framebackend.model.dto.entry.EntryResponse;
import com.dilem.framebackend.model.dto.entry.PagedEntriesResponse;
import com.dilem.framebackend.model.dto.entry.UpdateEntryRequest;
import com.dilem.framebackend.service.EntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @GetMapping
    public ResponseEntity<PagedEntriesResponse> getMyEntries(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String email = authentication.getName();
        Page<EntryResponse> entriesPage = entryService.getEntries(email, page, size);
        
        // --- DEBUG LOGLARI BAŞLANGICI ---
        System.out.println("=== BACKEND SYNC DEBUG ===");
        System.out.println("İstek atan kullanıcı: " + email);
        System.out.println("İstenen Sayfa: " + page + " | Boyut: " + size);
        System.out.println("Veritabanından dönen kayıt sayısı: " + entriesPage.getNumberOfElements());
        System.out.println("Daha fazla sayfa var mı (hasMore): " + entriesPage.hasNext());
        // --- DEBUG LOGLARI BİTİŞİ ---

        PagedEntriesResponse response = new PagedEntriesResponse(
                entriesPage.getContent(),
                entriesPage.getNumber(),
                entriesPage.hasNext()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntryResponse> getEntry(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        return ResponseEntity.ok(entryService.getEntry(email, id));
    }

    @PostMapping
    public ResponseEntity<EntryResponse> createEntry(
            Authentication authentication,
            @Valid @RequestBody CreateEntryRequest req
    ) {
        String email = authentication.getName();
        EntryResponse response = entryService.createEntry(email, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntryResponse> updateEntry(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateEntryRequest req
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(entryService.updateEntry(email, id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        entryService.deleteEntry(email, id);
        return ResponseEntity.noContent().build();
    }
}
