package com.notes.app.service;

import com.notes.app.dto.NoteDto;
import com.notes.app.entity.Note;
import com.notes.app.exception.NoteNotFoundException;
import com.notes.app.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    private Note savedNote;
    private NoteDto.Request validRequest;

    @BeforeEach
    void setUp() {
        savedNote = Note.builder()
                .id(1L)
                .title("Test Title")
                .content("Test content")
                .ownerUsername("alice")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = NoteDto.Request.builder()
                .title("Test Title")
                .content("Test content")
                .build();
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("saves note and returns response DTO")
        void create_savesNote_returnsResponse() {
            given(noteRepository.save(any(Note.class))).willReturn(savedNote);

            NoteDto.Response response = noteService.create(validRequest, "alice");

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Test Title");
            assertThat(response.getOwnerUsername()).isEqualTo("alice");
            then(noteRepository).should(times(1)).save(any(Note.class));
        }
    }

    // -------------------------------------------------------------------------
    // Get by ID
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns note when found")
        void getById_found_returnsDto() {
            given(noteRepository.findById(1L)).willReturn(Optional.of(savedNote));

            NoteDto.Response response = noteService.getById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Test Title");
        }

        @Test
        @DisplayName("throws NoteNotFoundException when not found")
        void getById_notFound_throws() {
            given(noteRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.getById(99L))
                    .isInstanceOf(NoteNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // -------------------------------------------------------------------------
    // Get all
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("returns all notes as DTOs")
        void getAll_returnsList() {
            given(noteRepository.findAll()).willReturn(List.of(savedNote));

            List<NoteDto.Response> result = noteService.getAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Test Title");
        }

        @Test
        @DisplayName("returns empty list when no notes exist")
        void getAll_empty_returnsEmptyList() {
            given(noteRepository.findAll()).willReturn(List.of());

            assertThat(noteService.getAll()).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates title and content, returns updated DTO")
        void update_existingNote_returnsUpdated() {
            NoteDto.Request updateReq = NoteDto.Request.builder()
                    .title("New Title")
                    .content("New content")
                    .build();

            Note updatedNote = Note.builder()
                    .id(1L).title("New Title").content("New content")
                    .ownerUsername("alice")
                    .createdAt(savedNote.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

            given(noteRepository.findById(1L)).willReturn(Optional.of(savedNote));
            given(noteRepository.save(any(Note.class))).willReturn(updatedNote);

            NoteDto.Response response = noteService.update(1L, updateReq);

            assertThat(response.getTitle()).isEqualTo("New Title");
            assertThat(response.getContent()).isEqualTo("New content");
        }

        @Test
        @DisplayName("throws NoteNotFoundException for unknown ID")
        void update_notFound_throws() {
            given(noteRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.update(99L, validRequest))
                    .isInstanceOf(NoteNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("deletes note when it exists")
        void delete_existingNote_callsDeleteById() {
            given(noteRepository.existsById(1L)).willReturn(true);
            willDoNothing().given(noteRepository).deleteById(1L);

            assertThatCode(() -> noteService.delete(1L)).doesNotThrowAnyException();
            then(noteRepository).should().deleteById(1L);
        }

        @Test
        @DisplayName("throws NoteNotFoundException when note does not exist")
        void delete_notFound_throws() {
            given(noteRepository.existsById(99L)).willReturn(false);

            assertThatThrownBy(() -> noteService.delete(99L))
                    .isInstanceOf(NoteNotFoundException.class)
                    .hasMessageContaining("99");
            then(noteRepository).should(never()).deleteById(anyLong());
        }
    }
}
