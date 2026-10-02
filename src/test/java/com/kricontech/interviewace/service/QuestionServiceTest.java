package com.kricontech.interviewace.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kricontech.interviewace.dto.QuestionResponse;
import com.kricontech.interviewace.model.QuestionCompletion;
import com.kricontech.interviewace.model.User;
import com.kricontech.interviewace.repository.QuestionCompletionRepository;
import com.kricontech.interviewace.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuestionCompletionRepository completionRepository;

    @InjectMocks
    private QuestionService questionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Casey", "casey@example.com", "hash");
    }

    @Test
    void getQuestionsFiltersCategoryAndMarksCompletions() {
        stubCaseyUser();
        when(completionRepository.findByUser(user))
                .thenReturn(List.of(new QuestionCompletion(user, 1L, "DSA")));

        List<QuestionResponse> questions = questionService.getQuestions("casey@example.com", "dsa");

        assertEquals(4, questions.size());
        assertTrue(questions.stream().filter(question -> question.getId().equals(1L))
                .findFirst().orElseThrow().isCompleted());
        assertFalse(questions.stream().filter(question -> question.getId().equals(2L))
                .findFirst().orElseThrow().isCompleted());
        assertTrue(questions.stream().allMatch(question -> question.getCategory().equals("DSA")));
    }

    @Test
    void getQuestionsWithoutCategoryReturnsAllQuestions() {
        stubCaseyUser();
        when(completionRepository.findByUser(user)).thenReturn(List.of());

        List<QuestionResponse> questions = questionService.getQuestions("casey@example.com", " ");

        assertEquals(12, questions.size());
        assertTrue(questions.stream().noneMatch(QuestionResponse::isCompleted));
    }

    @Test
    void getQuestionsRejectsUnknownUser() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertEquals("User not found.", assertThrows(IllegalArgumentException.class,
                () -> questionService.getQuestions("missing@example.com", null)).getMessage());
    }

    @Test
    void completeQuestionPersistsOnceAndUpdatesCategoryProgress() {
        stubCaseyUser();
        when(completionRepository.existsByUserAndQuestionId(user, 1L)).thenReturn(false);
        when(completionRepository.countByUserAndCategory(user, "DSA")).thenReturn(2L);

        Optional<QuestionResponse> result = questionService.completeQuestion("casey@example.com", "dsa", 1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.orElseThrow().getId());
        assertEquals(50, user.getDsaProgress());
        assertEquals(2, user.getDsaSolved());
        verify(completionRepository).save(any(QuestionCompletion.class));
        verify(userRepository).save(user);
    }

    @Test
    void completingAlreadyCompletedQuestionDoesNotWriteAgain() {
        stubCaseyUser();
        when(completionRepository.existsByUserAndQuestionId(user, 1L)).thenReturn(true);

        Optional<QuestionResponse> result = questionService.completeQuestion("casey@example.com", "DSA", 1L);

        assertTrue(result.isEmpty());
        verify(completionRepository, never()).save(any(QuestionCompletion.class));
        verify(userRepository, never()).save(user);
    }

    @Test
    void completeQuestionUpdatesPythonProgressAndCapsAtOneHundredPercent() {
        stubCaseyUser();
        when(completionRepository.existsByUserAndQuestionId(user, 5L)).thenReturn(false);
        when(completionRepository.countByUserAndCategory(user, "Python")).thenReturn(4L);

        Optional<QuestionResponse> result = questionService.completeQuestion("casey@example.com", "PYTHON", 5L);

        assertTrue(result.isPresent());
        assertEquals(100, user.getPythonProgress());
        assertEquals(0, user.getDsaSolved());
        assertEquals(0, user.getJavaQuestions());
    }

    @Test
    void completeQuestionRejectsUnknownQuestionOrCategory() {
        stubCaseyUser();
        assertEquals("Question not found.", assertThrows(IllegalArgumentException.class,
                () -> questionService.completeQuestion("casey@example.com", "DSA", 999L)).getMessage());
        assertEquals("Question not found.", assertThrows(IllegalArgumentException.class,
                () -> questionService.completeQuestion("casey@example.com", "Java", 1L)).getMessage());
        verify(completionRepository, never()).save(any(QuestionCompletion.class));
    }

    private void stubCaseyUser() {
        when(userRepository.findByEmail("casey@example.com")).thenReturn(Optional.of(user));
    }
}