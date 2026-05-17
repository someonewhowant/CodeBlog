package com.example.blog.controller;

import com.example.blog.entity.*;
import com.example.blog.service.CourseService;
import com.example.blog.service.FileStorageService;
import com.example.blog.service.QuizService;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherController {

    private final UserService userService;
    private final CourseService courseService;
    private final QuizService quizService;
    private final FileStorageService fileStorageService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        List<Course> courses = courseService.getCoursesByTeacher(user);
        model.addAttribute("user", user);
        model.addAttribute("courses", courses);
        model.addAttribute("title", "Teacher Dashboard");
        return "teacher/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("title", "Profile Settings");
        return "teacher/profile";
    }

    @PostMapping("/profile")
    @Transactional
    public String updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Principal principal) {
        userService.updateProfile(principal.getName(), fullName, phoneNumber, avatar);
        return "redirect:/teacher/profile?success";
    }

    // --- Course Management ---

    @GetMapping("/courses")
    public String listCourses(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("courses", courseService.getCoursesByTeacher(user));
        model.addAttribute("title", "My Courses");
        return "teacher/courses";
    }

    @GetMapping("/courses/add")
    public String addCourseForm(Model model) {
        model.addAttribute("title", "Create New Course");
        return "teacher/add-course";
    }

    @PostMapping("/courses/add")
    @Transactional
    public String addCourse(@ModelAttribute Course course,
                            @RequestParam("image") MultipartFile image,
                            Principal principal) throws IOException {
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        course.setTeacher(user);
        if (!image.isEmpty()) {
            course.setImageUrl(fileStorageService.storeFile(image));
        }
        courseService.createCourse(course);
        return "redirect:/teacher/courses";
    }

    @GetMapping("/courses/{id}/edit")
    public String editCourseForm(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        model.addAttribute("course", course);
        model.addAttribute("title", "Edit Course");
        return "teacher/edit-course";
    }

    @PostMapping("/courses/{id}/edit")
    @Transactional
    public String updateCourse(@PathVariable Long id,
                               @ModelAttribute Course courseDetails,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               Principal principal) throws IOException {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        
        if (image != null && !image.isEmpty()) {
            courseDetails.setImageUrl(fileStorageService.storeFile(image));
        } else {
            courseDetails.setImageUrl(course.getImageUrl());
        }
        
        courseService.updateCourse(id, courseDetails);
        return "redirect:/teacher/courses";
    }

    @GetMapping("/courses/{id}/delete")
    @Transactional
    public String deleteCourse(@PathVariable Long id, Principal principal) {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        courseService.deleteCourse(id);
        return "redirect:/teacher/courses";
    }

    // --- Builder Interface ---

    @GetMapping("/courses/{id}/builder")
    public String courseBuilder(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        model.addAttribute("course", course);
        model.addAttribute("quizzes", quizService.getQuizzesByCourseId(id));
        model.addAttribute("title", "Course Builder: " + course.getTitle());
        return "teacher/builder";
    }

    // --- Module Management ---

    @PostMapping("/courses/{id}/modules/add")
    @Transactional
    public String addModule(@PathVariable Long id,
                            @RequestParam("title") String title,
                            Principal principal) {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        
        CourseModule module = CourseModule.builder().title(title).build();
        courseService.addModule(id, module);
        return "redirect:/teacher/courses/" + id + "/builder";
    }

    @PostMapping("/courses/{id}/modules/{mid}/edit")
    @Transactional
    public String updateModule(@PathVariable Long id,
                               @PathVariable Long mid,
                               @RequestParam("title") String title,
                               @RequestParam("orderIndex") int orderIndex,
                               Principal principal) {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        
        CourseModule moduleDetails = CourseModule.builder()
                .title(title)
                .orderIndex(orderIndex)
                .build();
        courseService.updateModule(mid, moduleDetails);
        return "redirect:/teacher/courses/" + id + "/builder";
    }

    @GetMapping("/courses/{id}/modules/{mid}/delete")
    @Transactional
    public String deleteModule(@PathVariable Long id, @PathVariable Long mid, Principal principal) {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        courseService.deleteModule(mid);
        return "redirect:/teacher/courses/" + id + "/builder";
    }

    // --- Lesson Management ---

    @PostMapping("/courses/{id}/modules/{mid}/lessons/add")
    @Transactional
    public String addLesson(@PathVariable Long id,
                            @PathVariable Long mid,
                            @RequestParam("title") String title,
                            @RequestParam("type") LessonType type,
                            Principal principal) {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        
        Lesson lesson = Lesson.builder()
                .title(title)
                .type(type)
                .build();
        courseService.addLesson(mid, lesson);
        return "redirect:/teacher/courses/" + id + "/builder";
    }

    @GetMapping("/courses/{id}/modules/{mid}/lessons/{lid}/edit")
    public String editLessonForm(@PathVariable Long id,
                                 @PathVariable Long mid,
                                 @PathVariable Long lid,
                                 Model model,
                                 Principal principal) {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        
        Lesson lesson = courseService.getLessonById(lid);
        model.addAttribute("course", course);
        model.addAttribute("module", courseService.getModuleById(mid));
        model.addAttribute("lesson", lesson);
        model.addAttribute("quizzes", quizService.getQuizzesByCourseId(id));
        model.addAttribute("title", "Edit Lesson: " + lesson.getTitle());
        return "teacher/edit-lesson";
    }

    @PostMapping("/courses/{id}/modules/{mid}/lessons/{lid}/edit")
    @Transactional
    public String updateLesson(@PathVariable Long id,
                               @PathVariable Long mid,
                               @PathVariable Long lid,
                               @ModelAttribute Lesson lessonDetails,
                               @RequestParam(value = "quizId", required = false) Long quizId,
                               Principal principal) {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        
        courseService.updateLesson(lid, lessonDetails);
        if (lessonDetails.getType() == LessonType.TEST) {
            courseService.setLessonQuiz(lid, quizId);
        }
        
        return "redirect:/teacher/courses/" + id + "/builder";
    }

    @GetMapping("/courses/{id}/modules/{mid}/lessons/{lid}/delete")
    @Transactional
    public String deleteLesson(@PathVariable Long id, @PathVariable Long mid, @PathVariable Long lid, Principal principal) {
        Course course = courseService.getCourseById(id);
        checkOwnership(course, principal);
        courseService.deleteLesson(lid);
        return "redirect:/teacher/courses/" + id + "/builder";
    }

    private void checkOwnership(Course course, Principal principal) {
        if (course.getTeacher() == null || !course.getTeacher().getUsername().equals(principal.getName())) {
            throw new RuntimeException("Access denied: You don't own this course");
        }
    }
}
