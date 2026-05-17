package com.example.blog.service;

import com.example.blog.entity.Course;
import com.example.blog.entity.CourseModule;
import com.example.blog.entity.Lesson;
import com.example.blog.entity.User;
import java.util.List;

public interface CourseService {
    List<Course> getAllCourses();
    List<Course> getCoursesByTeacher(User teacher);
    Course getCourseById(Long id);
    Course createCourse(Course course);
    Course updateCourse(Long id, Course course);
    void deleteCourse(Long id);

    // Module management
    CourseModule addModule(Long courseId, CourseModule module);
    void deleteModule(Long moduleId);
    List<CourseModule> getModulesByCourseId(Long courseId);
    CourseModule getModuleById(Long moduleId);
    CourseModule updateModule(Long moduleId, CourseModule module);
    
    // Lesson management
    Lesson addLesson(Long moduleId, Lesson lesson);
    void deleteLesson(Long lessonId);
    List<Lesson> getLessonsByModuleId(Long moduleId);
    Lesson getLessonById(Long lessonId);
    Lesson updateLesson(Long lessonId, Lesson lesson);
    void setLessonQuiz(Long lessonId, Long quizId);
}
