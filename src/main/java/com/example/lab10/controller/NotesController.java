package com.example.lab10.controller;

import com.example.lab10.dto_.NoteRequest;
import com.example.lab10.entity.Note;
import com.example.lab10.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notes")
@PreAuthorize("isAuthenticated()")
public class NotesController {

    private final NoteService noteService;

    public NotesController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public List<Map<String, Object>> myNotes() {
        return noteService.getMyNotes()
                .stream()
                .map(this::noteToMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public Map<String, Object> countNotes() {
        int count = noteService.countMyNotes();
        return Map.of("count", count);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public List<Map<String, Object>> searchNotes(@RequestParam(name = "q", defaultValue = "") String keyword) {
        return noteService.searchByTitle(keyword)
                .stream()
                .map(this::noteToMap)
                .collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody NoteRequest req) {
        Note note = noteService.createNote(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("ok", true, "id", note.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getOne(@PathVariable Long id) {
        return noteService.getNoteById(id)
                .<ResponseEntity<Map<String, Object>>>map(note -> 
                        ResponseEntity.ok(noteToMap(note)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", 404, "error", "note_not_found")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                       @Valid @RequestBody NoteRequest req) {
        return noteService.updateNote(id, req)
                .<ResponseEntity<Map<String, Object>>>map(note -> 
                        ResponseEntity.ok(Map.of("ok", true)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", 404, "error", "note_not_found")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        if (noteService.deleteNote(id)) {
            return ResponseEntity.ok(Map.of("ok", true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", 404, "error", "note_not_found"));
    }

    private Map<String, Object> noteToMap(Note note) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", note.getId());
        m.put("title", note.getTitle());
        m.put("content", note.getContent());
        return m;
    }
}

