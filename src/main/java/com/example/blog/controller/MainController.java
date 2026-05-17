package com.example.blog.controller;

import com.example.blog.entity.*;
import com.example.blog.service.CourseService;
import com.example.blog.service.MarkdownService;
import com.example.blog.service.PostService;
import com.example.blog.service.QuizService;
import com.example.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PostService postService;
    private final CourseService courseService;
    private final QuizService quizService;
    private final MarkdownService markdownService;
    private final UserRepository userRepository;

    /**
     * Главная страница блога с пагинацией.
     */
    @GetMapping("")
    public String index(@RequestParam(defaultValue = "1") int page, Model model) {
        Page<Post> postPage = postService.getAllPosts(page);
        
        // Добавляем последние курсы (топ 3)
        List<Course> recentCourses = courseService.getAllCourses().stream()
                .limit(3)
                .collect(Collectors.toList());
        model.addAttribute("recentCourses", recentCourses);
        
        return populateModelAndReturn(postPage, page, "CodeBlog", "A modern blog platform built with Spring Boot 3.", model, "index");
    }

    /**
     * Все статьи блога с пагинацией.
     */
    @GetMapping("/articles")
    public String articles(@RequestParam(defaultValue = "1") int page, Model model) {
        Page<Post> postPage = postService.getAllPosts(page);
        return populateModelAndReturn(postPage, page, "Articles", "Explore all insights from our blog.", model, "articles");
    }

    /**
     * Просмотр конкретного поста по ID.
     */
    @GetMapping("/post/{id}")
    public String post(@PathVariable Long id, Model model) {
        Post post = postService.getPostById(id);
        String htmlBody = markdownService.convertToHtml(post.getBody());
        
        Post displayPost = Post.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(htmlBody)
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .category(post.getCategory())
                .tags(post.getTags())
                .build();

        model.addAttribute("post", displayPost);
        model.addAttribute("title", post.getTitle());
        
        return "post";
    }

    /**
     * Фильтрация по категории.
     */
    @GetMapping("/category/{slug}")
    public String category(@PathVariable String slug, 
                           @RequestParam(defaultValue = "1") int page, 
                           Model model) {
        Page<Post> postPage = postService.getPostsByCategory(slug, page);
        model.addAttribute("currentCategory", slug);
        return populateModelAndReturn(postPage, page, "Category: " + slug, null, model, "index");
    }

    /**
     * Фильтрация по тегу.
     */
    @GetMapping("/tag/{slug}")
    public String tag(@PathVariable String slug, 
                      @RequestParam(defaultValue = "1") int page, 
                      Model model) {
        Page<Post> postPage = postService.getPostsByTag(slug, page);
        return populateModelAndReturn(postPage, page, "Tag: " + slug, null, model, "index");
    }

    /**
     * Поиск постов по ключевому слову.
     */
    @GetMapping("/search")
    public String search(@RequestParam String searchTerm, 
                         @RequestParam(defaultValue = "1") int page, 
                         Model model) {
        Page<Post> searchResults = postService.searchPosts(searchTerm, page);
        
        List<Post> posts = searchResults.getContent().stream().map(this::mapPostForDisplay).collect(Collectors.toList());

        model.addAttribute("data", posts);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("title", "Search Results");
        
        return "search";
    }

    private String populateModelAndReturn(Page<Post> postPage, int page, String title, String description, Model model, String view) {
        List<Post> posts = postPage.getContent().stream().map(this::mapPostForDisplay).collect(Collectors.toList());

        model.addAttribute("data", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("title", title);
        if (description != null) {
            model.addAttribute("description", description);
        }
        
        return view;
    }

    private Post mapPostForDisplay(Post post) {
        return Post.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(markdownService.convertToHtml(post.getBody()).replaceAll("<[^>]*>", ""))
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .category(post.getCategory())
                .tags(post.getTags())
                .build();
    }

    /**
     * Страница "Courses".
     */
    @GetMapping("/courses")
    public String courses(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("title", "Courses");
        return "courses";
    }

    @GetMapping("/course/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        String htmlContent = markdownService.convertToHtml(course.getContent());
        
        model.addAttribute("course", course);
        model.addAttribute("htmlContent", htmlContent);
        model.addAttribute("title", course.getTitle());
        model.addAttribute("isOverview", true);
        model.addAttribute("isLocked", false);
        model.addAttribute("isNextLocked", false);
        return "course-detail";
    }

    /**
     * Страница конкретного модуля курса (по умолчанию первая лекция).
     */
    @GetMapping("/course/{courseId}/module/{moduleId}")
    public String moduleDetail(@PathVariable Long courseId, 
                               @PathVariable Long moduleId, 
                               @RequestParam(required = false) Long lessonId,
                               Principal principal,
                               Model model) {
        Course course = courseService.getCourseById(courseId);
        CourseModule module = courseService.getModuleById(moduleId);
        List<Lesson> lessons = module.getLessons();
        
        if (lessons.isEmpty()) {
            model.addAttribute("course", course);
            model.addAttribute("module", module);
            model.addAttribute("htmlContent", "<p>This module has no lessons yet.</p>");
            model.addAttribute("isOverview", false);
            model.addAttribute("isLocked", false);
            return "course-detail";
        }

        Lesson currentLesson = (lessonId != null) 
                ? lessons.stream().filter(l -> l.getId().equals(lessonId)).findFirst().orElse(lessons.get(0))
                : lessons.get(0);

        List<CourseModule> modules = course.getModules();
        CourseModule nextModule = null;
        CourseModule prevModule = null;
        
        boolean isLocked = false;
        String lockReason = "";
        
        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i).getId().equals(moduleId)) {
                if (i > 0) {
                    prevModule = modules.get(i - 1);
                    // Check if previous module had a TEST lesson and if it was passed
                    Lesson prevTest = prevModule.getLessons().stream()
                            .filter(l -> l.getType() == LessonType.TEST && l.getQuiz() != null)
                            .findFirst()
                            .orElse(null);
                    
                    if (prevTest != null && principal != null) {
                        User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                        if (!quizService.isQuizPassed(user.getId(), prevTest.getQuiz().getId())) {
                            isLocked = true;
                            lockReason = "You must pass the quiz in: " + prevModule.getTitle();
                        }
                    }
                }
                if (i < modules.size() - 1) nextModule = modules.get(i + 1);
                break;
            }
        }
        
        Lesson nextLesson = null;
        Lesson prevLesson = null;
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getId().equals(currentLesson.getId())) {
                if (i > 0) prevLesson = lessons.get(i - 1);
                if (i < lessons.size() - 1) nextLesson = lessons.get(i + 1);
                break;
            }
        }

        boolean isNextLocked = false;
        boolean isCurrentQuizPassed = false;
        if (currentLesson.getType() == LessonType.TEST && currentLesson.getQuiz() != null && principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                isCurrentQuizPassed = quizService.isQuizPassed(user.getId(), currentLesson.getQuiz().getId());
                if (!isCurrentQuizPassed) {
                    isNextLocked = true;
                }
            }
        }
        
        String htmlContent = (currentLesson.getContent() != null) 
                ? markdownService.convertToHtml(currentLesson.getContent())
                : "";
        
        model.addAttribute("course", course);
        model.addAttribute("module", module);
        model.addAttribute("lesson", currentLesson);
        model.addAttribute("htmlContent", htmlContent);
        model.addAttribute("nextModule", nextModule);
        model.addAttribute("prevModule", prevModule);
        model.addAttribute("nextLesson", nextLesson);
        model.addAttribute("prevLesson", prevLesson);
        model.addAttribute("isLocked", isLocked);
        model.addAttribute("isNextLocked", isNextLocked);
        model.addAttribute("isCurrentQuizPassed", isCurrentQuizPassed);
        model.addAttribute("lockReason", lockReason);
        model.addAttribute("title", currentLesson.getTitle() + " - " + course.getTitle());
        model.addAttribute("isOverview", false);
        
        return "course-detail";
    }

    /**
     * Страница прохождения квиза.
     */
    @GetMapping("/quiz/{id}")
    public String takeQuiz(@PathVariable Long id, 
                           @RequestParam(required = false) Long lessonId,
                           Model model) {
        Quiz quiz = quizService.getQuizById(id);
        model.addAttribute("quiz", quiz);
        model.addAttribute("lessonId", lessonId);
        model.addAttribute("title", "Quiz: " + quiz.getTitle());
        return "quiz";
    }

    /**
     * Обработка результатов квиза.
     */
    @PostMapping("/quiz/{id}/submit")
    public String submitQuiz(@PathVariable Long id, 
                             @RequestParam(required = false) Long lessonId,
                             @RequestParam Map<String, String> params, 
                             Principal principal,
                             Model model) {
        Quiz quiz = quizService.getQuizById(id);
        int correctAnswers = 0;
        int totalQuestions = quiz.getQuestions().size();
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Question question : quiz.getQuestions()) {
            String selectedOptionId = params.get("question_" + question.getId());
            boolean isCorrect = false;
            String correctAnswerText = "";
            String selectedAnswerText = "No answer";
            
            for (QuestionOption option : question.getOptions()) {
                if (option.isCorrect()) {
                    correctAnswerText = option.getText();
                }
                if (selectedOptionId != null && selectedOptionId.equals(option.getId().toString())) {
                    selectedAnswerText = option.getText();
                    if (option.isCorrect()) {
                        isCorrect = true;
                        correctAnswers++;
                    }
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("question", question.getText());
            result.put("selected", selectedAnswerText);
            result.put("correct", correctAnswerText);
            result.put("isCorrect", isCorrect);
            results.add(result);
        }
        
        boolean isPassed = false;
        // Save results if user is logged in
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            quizService.saveQuizResult(user.getId(), quiz.getId(), correctAnswers);
            isPassed = correctAnswers >= 3;
            model.addAttribute("isPassed", isPassed);
        }
        
        if (isPassed && lessonId != null) {
            Lesson currentLesson = courseService.getLessonById(lessonId);
            CourseModule module = currentLesson.getModule();
            List<Lesson> lessons = module.getLessons();
            Lesson nextLesson = null;
            for (int i = 0; i < lessons.size(); i++) {
                if (lessons.get(i).getId().equals(lessonId)) {
                    if (i < lessons.size() - 1) {
                        nextLesson = lessons.get(i + 1);
                    }
                    break;
                }
            }
            model.addAttribute("nextLesson", nextLesson);
            model.addAttribute("moduleId", module.getId());
            model.addAttribute("courseId", module.getCourse().getId());
        }
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("lessonId", lessonId);
        model.addAttribute("score", correctAnswers);
        model.addAttribute("total", totalQuestions);
        model.addAttribute("results", results);
        model.addAttribute("title", "Results: " + quiz.getTitle());
        model.addAttribute("submitted", true);
        
        return "quiz";
    }

    /**
     * Статическая страница "About".
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About Us");
        return "about";
    }
}
