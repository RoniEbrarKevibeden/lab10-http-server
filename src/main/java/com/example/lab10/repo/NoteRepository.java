package com.example.lab10.repo;

import com.example.lab10.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserUsername(String username);
    Optional<Note> findByIdAndUserUsername(Long id, String username);
}
