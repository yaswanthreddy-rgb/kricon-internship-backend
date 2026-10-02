package com.kricontech.interviewace.repository;

import com.kricontech.interviewace.model.QuestionCompletion;
import com.kricontech.interviewace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionCompletionRepository extends JpaRepository<QuestionCompletion, Long> {
    boolean existsByUserAndQuestionId(User user, Long questionId);
    long countByUserAndCategory(User user, String category);
    List<QuestionCompletion> findByUser(User user);
}
