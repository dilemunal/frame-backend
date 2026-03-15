package com.dilem.framebackend.repository;

import com.dilem.framebackend.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<JournalEntry> findByUserEmailOrderByCreatedAtDesc(String email);
}
