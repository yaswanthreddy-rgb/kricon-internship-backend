package com.kricontech.interviewace.service;

import com.kricontech.interviewace.dto.QuestionResponse;
import com.kricontech.interviewace.model.QuestionCompletion;
import com.kricontech.interviewace.model.User;
import com.kricontech.interviewace.repository.QuestionCompletionRepository;
import com.kricontech.interviewace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final UserRepository userRepository;
    private final QuestionCompletionRepository completionRepository;

    private final List<QuestionResponse> questions = List.of(
            new QuestionResponse(1L, "DSA", "Two Sum", "Given an integer array and a target, find two indices whose values add up to the target."),
            new QuestionResponse(2L, "DSA", "Binary Search", "Explain how binary search works and its time complexity on a sorted array."),
            new QuestionResponse(3L, "Java", "OOP Principles", "Explain encapsulation, inheritance, polymorphism and abstraction with examples."),
            new QuestionResponse(4L, "Java", "ArrayList vs LinkedList", "Compare ArrayList and LinkedList and explain when you would use each."),
            new QuestionResponse(5L, "Python", "List vs Tuple", "Explain the difference between a Python list and tuple and give a use case for each."),
            new QuestionResponse(6L, "Python", "Dictionary", "What is a Python dictionary and how does key-based lookup work?"),
            new QuestionResponse(7L, "DSA", "Valid Parentheses", "Describe an approach to check whether brackets in a string are balanced."),
            new QuestionResponse(8L, "DSA", "Maximum Depth", "Explain how you would find the maximum depth of a binary tree."),
            new QuestionResponse(9L, "Java", "HashMap", "Explain how HashMap stores and retrieves key-value pairs."),
            new QuestionResponse(10L, "Java", "Exception Handling", "Explain checked and unchecked exceptions in Java."),
            new QuestionResponse(11L, "Python", "List Comprehension", "What is list comprehension and when is it useful?"),
            new QuestionResponse(12L, "Python", "Functions", "Explain positional arguments, keyword arguments and default arguments in Python.")
    );

    public QuestionService(UserRepository userRepository, QuestionCompletionRepository completionRepository) {
        this.userRepository = userRepository;
        this.completionRepository = completionRepository;
    }

    /** Questions for the logged-in user, each flagged with whether they already completed it. */
    public List<QuestionResponse> getQuestions(String email, String category) {
        User user = findUser(email);
        Set<Long> doneIds = completionRepository.findByUser(user).stream()
                .map(QuestionCompletion::getQuestionId)
                .collect(Collectors.toSet());

        return questions.stream()
                .filter(q -> category == null || category.isBlank() || q.getCategory().equalsIgnoreCase(category))
                .map(q -> q.withCompleted(doneIds.contains(q.getId())))
                .toList();
    }

    /**
     * Marks a question complete once per user.
     * Returns the question if it was newly completed, or empty if it had already been completed.
     */
    @Transactional
    public Optional<QuestionResponse> completeQuestion(String email, String category, Long questionId) {
        User user = findUser(email);

        QuestionResponse question = questions.stream()
                .filter(q -> q.getId().equals(questionId) && q.getCategory().equalsIgnoreCase(category))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Question not found."));

        if (completionRepository.existsByUserAndQuestionId(user, questionId)) {
            return Optional.empty();
        }

        completionRepository.save(new QuestionCompletion(user, questionId, question.getCategory()));

        long done = completionRepository.countByUserAndCategory(user, question.getCategory());
        int progress = progress(done, question.getCategory());

        switch (question.getCategory().toUpperCase()) {
            case "DSA" -> {
                user.setDsaSolved((int) done);
                user.setDsaProgress(progress);
            }
            case "JAVA" -> {
                user.setJavaQuestions((int) done);
                user.setJavaProgress(progress);
            }
            case "PYTHON" -> user.setPythonProgress(progress);
            default -> throw new IllegalArgumentException("Unsupported question category.");
        }
        userRepository.save(user);
        return Optional.of(question);
    }

    /** Progress = completed / total questions in that category, so it reaches exactly 100%. */
    private int progress(long completed, String category) {
        long total = questions.stream().filter(q -> q.getCategory().equalsIgnoreCase(category)).count();
        if (total == 0) return 0;
        return (int) Math.min(100, Math.round(completed * 100.0 / total));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }
}
