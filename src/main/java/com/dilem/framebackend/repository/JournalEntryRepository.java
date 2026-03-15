package com.dilem.framebackend.repository;

import com.dilem.framebackend.model.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    @Query("SELECT e FROM JournalEntry e WHERE e.user.id = :userId ORDER BY e.createdAt DESC")
    Page<JournalEntry> findByUserId(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT e FROM JournalEntry e WHERE e.id = :id AND e.user.id = :userId")
    Optional<JournalEntry> findByIdAndUserId(@Param("id") Long id, @Param("userId") Integer userId);

    @Modifying
    @Query("DELETE FROM JournalEntry e WHERE e.id = :id AND e.user.id = :userId")
    void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Integer userId);

    @Query("SELECT e FROM JournalEntry e WHERE e.user.id = :userId " +
           "AND e.createdAt BETWEEN :from AND :to")
    List<JournalEntry> findByUserIdAndCreatedAtBetween(
        @Param("userId") Integer userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query("SELECT e FROM JournalEntry e WHERE e.user.email = :email ORDER BY e.createdAt DESC")
    List<JournalEntry> findByUserEmailOrderByCreatedAtDesc(@Param("email") String email);
}
