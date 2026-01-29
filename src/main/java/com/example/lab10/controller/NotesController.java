package com.example.lab10.controller;

import com.example.lab10.dto_.NoteRequest;
import com.example.lab10.entity.Note;
import com.example.lab10.repo.NoteRepository;
import com.example.lab10.repo.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notes")
public class NotesController {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NotesController(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // ✅ GET my notes
    @GetMapping
    public List<Map<String, Object>> myNotes() {
        String username = currentUsername();

        return noteRepository.findByUserUsername(username)
                .stream()
                .map(n -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", n.getId());
                    m.put("title", n.getTitle());
                    m.put("content", n.getContent());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // CREATE note
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody NoteRequest req) {
        String username = currentUsername();

        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "error", "user_not_found"));
        }

        Note n = new Note();
        n.setUser(userOpt.get());
        n.setTitle(req.getTitle());
        n.setContent(req.getContent());

        noteRepository.save(n);

        return ResponseEntity.ok(Map.of("ok", true, "id", n.getId()));
    }

    // ✅ GET note by id (only owner)
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        String username = currentUsername();

        return noteRepository.findByIdAndUserUsername(id, username)
                .<ResponseEntity<?>>map(note -> ResponseEntity.ok(Map.of(
                        "id", note.getId(),
                        "title", note.getTitle(),
                        "content", note.getContent()
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", 404, "error", "note_not_found")));
    }

    // ✅ UPDATE (only owner)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody NoteRequest req) {
        String username = currentUsername();

        var opt = noteRepository.findByIdAndUserUsername(id, username);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "error", "note_not_found"));
        }

        var note = opt.get();
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());
        noteRepository.save(note);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ✅ DELETE (only owner)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        String username = currentUsername();

        var opt = noteRepository.findByIdAndUserUsername(id, username);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "error", "note_not_found"));
        }

        noteRepository.delete(opt.get());

        return ResponseEntity.ok(Map.of("ok", true));
    }
}

