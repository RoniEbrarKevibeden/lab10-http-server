package com.example.lab10.repo;

import com.example.lab10.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserUsername(String username);
    Optional<Note> findByIdAndUserUsername(Long id, String username);

    @Query(value = "SELECT * FROM notes WHERE user_id = :userId ORDER BY id DESC", nativeQuery = true)
    List<Note> findByUserIdNative(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM notes WHERE user_id = :userId", nativeQuery = true)
    int countByUserIdNative(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM notes WHERE user_id = :userId AND title LIKE '%' || :keyword || '%'", nativeQuery = true)
    List<Note> searchByTitleNative(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Modifying
    @Query(value = "DELETE FROM notes WHERE id = :noteId AND user_id = :userId", nativeQuery = true)
    int deleteByIdAndUserIdNative(@Param("noteId") Long noteId, @Param("userId") Long userId);
}
