package com.example.lab10.service;

import com.example.lab10.dto_.NoteRequest;
import com.example.lab10.entity.AppUser;
import com.example.lab10.entity.Note;
import com.example.lab10.repo.NoteRepository;
import com.example.lab10.repo.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .map(AppUser::getId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    public Optional<AppUser> getCurrentUser() {
        return userRepository.findByUsername(getCurrentUsername());
    }

    @Transactional(readOnly = true)
    public List<Note> getMyNotes() {
        return noteRepository.findByUserUsername(getCurrentUsername());
    }

    @Transactional(readOnly = true)
    public List<Note> getMyNotesNative() {
        return noteRepository.findByUserIdNative(getCurrentUserId());
    }

    @Transactional(readOnly = true)
    public int countMyNotes() {
        return noteRepository.countByUserIdNative(getCurrentUserId());
    }

    @Transactional(readOnly = true)
    public List<Note> searchByTitle(String keyword) {
        return noteRepository.searchByTitleNative(getCurrentUserId(), keyword);
    }

    @Transactional(readOnly = true)
    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findByIdAndUserUsername(id, getCurrentUsername());
    }

    public Note createNote(NoteRequest request) {
        AppUser user = getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Note note = new Note();
        note.setUser(user);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());

        return noteRepository.save(note);
    }

    public Optional<Note> updateNote(Long id, NoteRequest request) {
        return noteRepository.findByIdAndUserUsername(id, getCurrentUsername())
                .map(note -> {
                    note.setTitle(request.getTitle());
                    note.setContent(request.getContent());
                    return noteRepository.save(note);
                });
    }

    public boolean deleteNote(Long id) {
        return noteRepository.findByIdAndUserUsername(id, getCurrentUsername())
                .map(note -> {
                    noteRepository.delete(note);
                    return true;
                })
                .orElse(false);
    }

    public boolean deleteNoteNative(Long id) {
        int deleted = noteRepository.deleteByIdAndUserIdNative(id, getCurrentUserId());
        return deleted > 0;
    }

    @Transactional(readOnly = true)
    public boolean isNoteOwnedByCurrentUser(Long noteId) {
        return noteRepository.findByIdAndUserUsername(noteId, getCurrentUsername()).isPresent();
    }
}
