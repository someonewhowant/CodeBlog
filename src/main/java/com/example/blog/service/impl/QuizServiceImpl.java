package com.example.blog.service.impl;

import com.example.blog.entity.Course;
import com.example.blog.entity.Quiz;
import com.example.blog.entity.Question;
import com.example.blog.repository.CourseRepository;
import com.example.blog.repository.QuizRepository;
import com.example.blog.repository.QuestionRepository;
import com.example.blog.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;

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
}
