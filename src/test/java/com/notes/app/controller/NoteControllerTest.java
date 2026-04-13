package com.notes.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notes.app.dto.NoteDto;
import com.notes.app.exception.NoteNotFoundException;
import com.notes.app.security.JwtAuthenticationFilter;
import com.notes.app.security.JwtUtils;
import com.notes.app.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = NoteController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteService noteService;

    // Needed by SecurityConfig even in slice tests
    @MockBean
    private JwtUtils jwtUtils;

    private NoteDto.Response sampleResponse;
    private NoteDto.Request validRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = NoteDto.Response.builder()
                .id(1L)
                .title("Spring Boot")
                .content("Great framework")
                .ownerUsername("alice")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = NoteDto.Request.builder()
                .title("Spring Boot")
                .content("Great framework")
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/notes
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/notes")
    class CreateNote {

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("returns 201 with created note")
        void create_validRequest_returns201() throws Exception {
            given(noteService.create(any(), eq("alice"))).willReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/notes")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Spring Boot"))
                    .andExpect(jsonPath("$.ownerUsername").value("alice"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when title is blank")
        void create_blankTitle_returns400() throws Exception {
            NoteDto.Request bad = NoteDto.Request.builder()
                    .title("")
                    .content("some content")
                    .build();

            mockMvc.perform(post("/api/v1/notes")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details.title").exists());
        }

        @Test
        @DisplayName("returns 401 when not authenticated")
        void create_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/notes")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/notes
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/notes")
    class GetAllNotes {

        @Test
        @WithMockUser
        @DisplayName("returns 200 with list of notes")
        void getAll_returns200WithList() throws Exception {
            given(noteService.getAll()).willReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/notes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("Spring Boot"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 with empty list when no notes")
        void getAll_empty_returns200EmptyList() throws Exception {
            given(noteService.getAll()).willReturn(List.of());

            mockMvc.perform(get("/api/v1/notes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/notes/{id}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/notes/{id}")
    class GetNoteById {

        @Test
        @WithMockUser
        @DisplayName("returns 200 with note when found")
        void getById_found_returns200() throws Exception {
            given(noteService.getById(1L)).willReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/notes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Spring Boot"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when note not found")
        void getById_notFound_returns404() throws Exception {
            given(noteService.getById(99L)).willThrow(new NoteNotFoundException(99L));

            mockMvc.perform(get("/api/v1/notes/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("99")));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/notes/search?title=...
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/notes/search")
    class SearchNotes {

        @Test
        @WithMockUser
        @DisplayName("returns 200 with matching notes")
        void search_returns200WithMatches() throws Exception {
            given(noteService.searchByTitle("spring")).willReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/notes/search").param("title", "spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("Spring Boot"));
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/notes/{id}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PUT /api/v1/notes/{id}")
    class UpdateNote {

        @Test
        @WithMockUser
        @DisplayName("returns 200 with updated note")
        void update_valid_returns200() throws Exception {
            NoteDto.Response updated = NoteDto.Response.builder()
                    .id(1L).title("Updated").content("Updated content")
                    .ownerUsername("alice")
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();

            given(noteService.update(eq(1L), any())).willReturn(updated);

            NoteDto.Request req = NoteDto.Request.builder()
                    .title("Updated").content("Updated content").build();

            mockMvc.perform(put("/api/v1/notes/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when note to update not found")
        void update_notFound_returns404() throws Exception {
            given(noteService.update(eq(99L), any())).willThrow(new NoteNotFoundException(99L));

            mockMvc.perform(put("/api/v1/notes/99")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when content is blank")
        void update_blankContent_returns400() throws Exception {
            NoteDto.Request bad = NoteDto.Request.builder()
                    .title("Valid title").content("").build();

            mockMvc.perform(put("/api/v1/notes/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/notes/{id}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("DELETE /api/v1/notes/{id}")
    class DeleteNote {

        @Test
        @WithMockUser
        @DisplayName("returns 204 on successful delete")
        void delete_existing_returns204() throws Exception {
            willDoNothing().given(noteService).delete(1L);

            mockMvc.perform(delete("/api/v1/notes/1").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when note to delete not found")
        void delete_notFound_returns404() throws Exception {
            willThrow(new NoteNotFoundException(99L)).given(noteService).delete(99L);

            mockMvc.perform(delete("/api/v1/notes/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 401 when not authenticated")
        void delete_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete("/api/v1/notes/1").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }
}
