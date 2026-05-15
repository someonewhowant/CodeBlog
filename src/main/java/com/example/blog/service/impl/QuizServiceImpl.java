package com.example.blog.service.impl;

import com.example.blog.entity.Course;
import com.example.blog.entity.Quiz;
import com.example.blog.entity.Question;
import com.example.blog.entity.QuestionOption;
import com.example.blog.entity.User;
import com.example.blog.entity.UserQuizResult;
import com.example.blog.repository.CourseRepository;
import com.example.blog.repository.QuizRepository;
import com.example.blog.repository.QuestionRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.repository.UserQuizResultRepository;
import com.example.blog.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserQuizResultRepository userQuizResultRepository;

    @Override
    public List<Quiz> getQuizzesByCourseId(Long courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    @Override
    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
    }

    @Override
    @Transactional
    public Quiz createQuiz(Long courseId, Quiz quiz) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        quiz.setCourse(course);
        return quizRepository.save(quiz);
    }

    @Override
    @Transactional
    public Quiz updateQuiz(Long id, Quiz quizDetails) {
        Quiz quiz = getQuizById(id);
        quiz.setTitle(quizDetails.getTitle());
        return quizRepository.save(quiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Question addQuestion(Long quizId, Question question) {
        Quiz quiz = getQuizById(quizId);
        question.setQuiz(quiz);
        if (question.getOptions() != null) {
            question.getOptions().forEach(option -> option.setQuestion(question));
        }
        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    @Override
    @Transactional
    public void saveQuizResult(Long userId, Long quizId, int score) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Quiz quiz = getQuizById(quizId);
        
        UserQuizResult result = userQuizResultRepository.findByUserIdAndQuizId(userId, quizId)
                .orElse(new UserQuizResult());
        
        result.setUser(user);
        result.setQuiz(quiz);
        // We save the best score
        if (score > result.getScore()) {
            result.setScore(score);
        }
        
        userQuizResultRepository.save(result);
    }

    @Override
    public int getUserScore(Long userId, Long quizId) {
        return userQuizResultRepository.findByUserIdAndQuizId(userId, quizId)
                .map(UserQuizResult::getScore)
                .orElse(0);
    }

    @Override
    public boolean isQuizPassed(Long userId, Long quizId) {
        return getUserScore(userId, quizId) >= 3;
    }

    @Override
    @Transactional
    public Quiz importQuizFromMarkdown(Long courseId, String content) {
        String[] lines = content.split("\n");
        Quiz quiz = new Quiz();
        List<Question> questions = new ArrayList<>();
        Question currentQuestion = null;

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;

            if (trimmedLine.startsWith("# ")) {
                if (quiz.getTitle() == null) {
                    quiz.setTitle(trimmedLine.substring(2).trim());
                }
            } else if (trimmedLine.startsWith("## ")) {
                currentQuestion = new Question();
                currentQuestion.setText(trimmedLine.substring(3).trim());
                currentQuestion.setQuiz(quiz);
                currentQuestion.setOptions(new ArrayList<>());
                questions.add(currentQuestion);
            } else if (trimmedLine.startsWith("- [ ] ") || trimmedLine.startsWith("- [x] ") || 
                       trimmedLine.startsWith("- [] ")) {
                if (currentQuestion != null) {
                    QuestionOption option = new QuestionOption();
                    boolean isCorrect = trimmedLine.startsWith("- [x] ");
                    String optionText = trimmedLine.substring(6).trim();
                    option.setText(optionText);
                    option.setCorrect(isCorrect);
                    option.setQuestion(currentQuestion);
                    currentQuestion.getOptions().add(option);
                }
            }
        }

        if (quiz.getTitle() == null) {
            quiz.setTitle("Imported Quiz");
        }

        quiz.setQuestions(questions);
        return createQuiz(courseId, quiz);
    }
}
