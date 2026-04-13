package com.notes.app.repository;

import com.notes.app.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * Case-insensitive title search using JPA derived query.
     * MySQL's default utf8mb4_general_ci collation makes LIKE case-insensitive natively;
     * Spring Data's IgnoreCase ensures portability across databases.
     */
    List<Note> findByTitleContainingIgnoreCase(String title);

    /**
     * Prepared for future user-ownership filtering.
     */
    List<Note> findByOwnerUsername(String ownerUsername);
}
