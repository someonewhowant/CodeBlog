package com.example.blog.service.impl;

import com.example.blog.entity.*;
import com.example.blog.repository.CourseModuleRepository;
import com.example.blog.repository.CourseRepository;
import com.example.blog.repository.LessonRepository;
import com.example.blog.repository.QuizRepository;
import com.example.blog.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public List<Course> getCoursesByTeacher(User teacher) {
        return courseRepository.findByTeacher(teacher);
    }

    @Override
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    @Override
    @Transactional
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        Course course = getCourseById(id);
        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setContent(courseDetails.getContent());
        course.setImageUrl(courseDetails.getImageUrl());
        course.setLevel(courseDetails.getLevel());
        course.setDuration(courseDetails.getDuration());
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public CourseModule addModule(Long courseId, CourseModule module) {
        Course course = getCourseById(courseId);
        module.setCourse(course);
        
        // Auto-set order index if not set
        if (module.getOrderIndex() == 0) {
            int maxOrder = course.getModules().stream()
                    .mapToInt(CourseModule::getOrderIndex)
                    .max()
                    .orElse(-1);
            module.setOrderIndex(maxOrder + 1);
        }
        
        return moduleRepository.save(module);
    }

    @Override
    @Transactional
    public void deleteModule(Long moduleId) {
        moduleRepository.deleteById(moduleId);
    }

    @Override
    public List<CourseModule> getModulesByCourseId(Long courseId) {
        return moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    @Override
    public CourseModule getModuleById(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found with id: " + moduleId));
    }

    @Override
    @Transactional
    public CourseModule updateModule(Long moduleId, CourseModule moduleDetails) {
        CourseModule module = getModuleById(moduleId);
        module.setTitle(moduleDetails.getTitle());
        module.setOrderIndex(moduleDetails.getOrderIndex());
        return moduleRepository.save(module);
    }

    @Override
    @Transactional
    public Lesson addLesson(Long moduleId, Lesson lesson) {
        CourseModule module = getModuleById(moduleId);
        lesson.setModule(module);

        if (lesson.getOrderIndex() == 0) {
            int maxOrder = module.getLessons().stream()
                    .mapToInt(Lesson::getOrderIndex)
                    .max()
                    .orElse(-1);
            lesson.setOrderIndex(maxOrder + 1);
        }

        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    @Override
    public List<Lesson> getLessonsByModuleId(Long moduleId) {
        return lessonRepository.findByModuleIdOrderByOrderIndexAsc(moduleId);
    }

    @Override
    public Lesson getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + lessonId));
    }

    @Override
    @Transactional
    public Lesson updateLesson(Long lessonId, Lesson lessonDetails) {
        Lesson lesson = getLessonById(lessonId);
        lesson.setTitle(lessonDetails.getTitle());
        lesson.setContent(lessonDetails.getContent());
        lesson.setType(lessonDetails.getType());
        lesson.setOrderIndex(lessonDetails.getOrderIndex());
        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public void setLessonQuiz(Long lessonId, Long quizId) {
        Lesson lesson = getLessonById(lessonId);
        if (quizId != null) {
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));
            lesson.setQuiz(quiz);
        } else {
            lesson.setQuiz(null);
        }
        lessonRepository.save(lesson);
    }
}
