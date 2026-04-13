package com.notes.app.service;

import com.notes.app.dto.NoteDto;
import com.notes.app.entity.Note;
import com.notes.app.exception.NoteNotFoundException;
import com.notes.app.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {

    private final NoteRepository noteRepository;

    /**
     * Creates a new note, stamping the authenticated user as owner.
     */
    @Transactional
    public NoteDto.Response create(NoteDto.Request request, String ownerUsername) {
        Note note = Note.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .ownerUsername(ownerUsername)
                .build();
        return toResponse(noteRepository.save(note));
    }

    /**
     * Returns all notes. In a multi-user system this would be filtered by owner.
     */
    public List<NoteDto.Response> getAll() {
        return noteRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns a single note by ID, or throws if not found.
     */
    public NoteDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Case-insensitive title search.
     */
    public List<NoteDto.Response> searchByTitle(String title) {
        return noteRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Updates title and content of an existing note.
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public NoteDto.Response update(Long id, NoteDto.Request request) {
        Note note = findOrThrow(id);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        return toResponse(noteRepository.save(note));
    }

    /**
     * Deletes a note by ID, or throws if not found.
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void delete(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException(id);
        }
        noteRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Note findOrThrow(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    private NoteDto.Response toResponse(Note note) {
        return NoteDto.Response.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .ownerUsername(note.getOwnerUsername())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
