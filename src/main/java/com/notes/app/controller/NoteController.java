package com.notes.app.controller;

import com.notes.app.dto.NoteDto;
import com.notes.app.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // POST /api/v1/notes
    @PostMapping
    public ResponseEntity<NoteDto.Response> create(
            @Valid @RequestBody NoteDto.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        NoteDto.Response created = noteService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /api/v1/notes
    @GetMapping
    public ResponseEntity<List<NoteDto.Response>> getAll() {
        return ResponseEntity.ok(noteService.getAll());
    }

    // GET /api/v1/notes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<NoteDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getById(id));
    }

    // GET /api/v1/notes/search?title=...
    @GetMapping("/search")
    public ResponseEntity<List<NoteDto.Response>> search(@RequestParam String title) {
        return ResponseEntity.ok(noteService.searchByTitle(title));
    }

    // PUT /api/v1/notes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<NoteDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody NoteDto.Request request) {
        return ResponseEntity.ok(noteService.update(id, request));
    }

    // DELETE /api/v1/notes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
