package com.dilem.framebackend.repository;

import com.dilem.framebackend.model.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    Page<JournalEntry> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);
    Optional<JournalEntry> findByIdAndUserId(Long id, Integer userId);
    void deleteByIdAndUserId(Long id, Integer userId);
    List<JournalEntry> findByUserIdAndCreatedAtBetween(Integer userId, LocalDateTime from, LocalDateTime to);
    List<JournalEntry> findByUserEmailOrderByCreatedAtDesc(String email); // Needed by old EntryController
}
