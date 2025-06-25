package com.popoworld.backend.quiz.child.repository;

import com.popoworld.backend.quiz.child.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuizDataRepository extends JpaRepository<Quiz, UUID> {
    Optional<Quiz> findByUserIdAndDifficultyAndTopic(UUID userId, String difficulty, String topic);
}
