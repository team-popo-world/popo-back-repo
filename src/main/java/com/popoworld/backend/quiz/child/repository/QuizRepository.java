package com.popoworld.backend.quiz.child.repository;

import com.popoworld.backend.quiz.child.entity.QuizHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuizRepository extends MongoRepository<QuizHistory, UUID> {
}
