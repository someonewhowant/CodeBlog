package com.example.blog.controller;

import com.example.blog.entity.Category;
import com.example.blog.entity.Course;
import com.example.blog.entity.CourseModule;
import com.example.blog.entity.Post;
import com.example.blog.service.CourseService;
import com.example.blog.service.FileStorageService;
import com.example.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PostService postService;
    private final CourseService courseService;
    private final FileStorageService fileStorageService;

    /**
     * Страница логина админа.
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Admin Login");
        return "admin/index";
    }

    /**
     * Панель управления (Dashboard).
     * Отображает список всех постов для редактирования/удаления.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("data", posts);
        model.addAttribute("title", "Dashboard");
        return "admin/dashboard";
    }

    /**
     * Форма добавления нового поста.
     */
    @GetMapping("/add-post")
    public String addPostForm(Model model) {
        model.addAttribute("title", "Add Post");
        model.addAttribute("categories", postService.getAllCategories());
        model.addAttribute("tags", postService.getAllTags());
        return "admin/add-post";
    }

    /**
     * Обработка создания нового поста.
     */
    @PostMapping("/add-post")
    public String addPost(@ModelAttribute Post post, 
                          @RequestParam("image") MultipartFile image,
                          @RequestParam(value = "markdownFile", required = false) MultipartFile markdownFile,
                          @RequestParam(value = "tagIds", required = false) List<Long> tagIds) throws IOException {
        if (!image.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(image);
            post.setImageUrl(imageUrl);
        }

        if (markdownFile != null && !markdownFile.isEmpty()) {
            String content = new String(markdownFile.getBytes(), StandardCharsets.UTF_8);
            post.setBody(content);
        }
        
        if (tagIds != null) {
            post.setTags(new java.util.HashSet<>(postService.getAllTags().stream()
                    .filter(t -> tagIds.contains(t.getId()))
                    .collect(java.util.stream.Collectors.toList())));
        }
        
        postService.createPost(post);
        return "redirect:/admin/dashboard";
    }

    /**
     * Форма редактирования поста.
     */
    @GetMapping("/edit-post/{id}")
    public String editPostForm(@PathVariable Long id, Model model) {
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);
        model.addAttribute("categories", postService.getAllCategories());
        model.addAttribute("tags", postService.getAllTags());
        model.addAttribute("title", "Edit Post");
        return "admin/edit-post";
    }

    /**
     * Обработка обновления поста.
     */
    @PostMapping("/edit-post/{id}")
    public String updatePost(@PathVariable Long id, 
                             @ModelAttribute Post post, 
                             @RequestParam("image") MultipartFile image,
                             @RequestParam(value = "markdownFile", required = false) MultipartFile markdownFile,
                             @RequestParam(value = "tagIds", required = false) List<Long> tagIds) throws IOException {
        if (!image.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(image);
            post.setImageUrl(imageUrl);
        }

        if (markdownFile != null && !markdownFile.isEmpty()) {
            String content = new String(markdownFile.getBytes(), StandardCharsets.UTF_8);
            post.setBody(content);
        }

        if (tagIds != null) {
            post.setTags(new java.util.HashSet<>(postService.getAllTags().stream()
                    .filter(t -> tagIds.contains(t.getId()))
                    .collect(java.util.stream.Collectors.toList())));
        } else {
            post.setTags(new java.util.HashSet<>());
        }
        
        postService.updatePost(id, post);
        return "redirect:/admin/dashboard";
    }

    /**
     * Удаление поста.
     */
    @GetMapping("/delete-post/{id}")
    public String deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/admin/dashboard";
    }

    /**
     * Управление категориями.
     */
    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", postService.getAllCategories());
        model.addAttribute("title", "Manage Categories");
        return "admin/categories";
    }

    /**
     * Добавление новой категории.
     */
    @PostMapping("/categories")
    public String addCategory(@ModelAttribute Category category) {
        postService.createCategory(category);
        return "redirect:/admin/categories";
    }

    /**
     * Удаление категории.
     */
    @GetMapping("/delete-category/{id}")
    public String deleteCategory(@PathVariable Long id) {
        postService.deleteCategory(id);
        return "redirect:/admin/categories";
    }

    /**
     * Управление курсами.
     */
    @GetMapping("/courses")
    public String manageCourses(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("title", "Manage Courses");
        return "admin/courses";
    }

    /**
     * Форма добавления курса.
     */
    @GetMapping("/add-course")
    public String addCourseForm(Model model) {
        model.addAttribute("title", "Add Course");
        return "admin/add-course";
    }

    /**
     * Обработка создания курса.
     */
    @PostMapping("/add-course")
    public String addCourse(@ModelAttribute Course course, 
                            @RequestParam("image") MultipartFile image,
                            @RequestParam(value = "markdownFile", required = false) MultipartFile markdownFile) throws IOException {
        if (!image.isEmpty()) {
            course.setImageUrl(fileStorageService.storeFile(image));
        }
        if (markdownFile != null && !markdownFile.isEmpty()) {
            course.setContent(new String(markdownFile.getBytes(), StandardCharsets.UTF_8));
        }
        courseService.createCourse(course);
        return "redirect:/admin/courses";
    }

    /**
     * Форма редактирования курса.
     */
    @GetMapping("/edit-course/{id}")
    public String editCourseForm(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.getCourseById(id));
        model.addAttribute("title", "Edit Course");
        return "admin/edit-course";
    }

    /**
     * Обработка обновления курса.
     */
    @PostMapping("/edit-course/{id}")
    public String updateCourse(@PathVariable Long id, 
                               @ModelAttribute Course course, 
                               @RequestParam("image") MultipartFile image,
                               @RequestParam(value = "markdownFile", required = false) MultipartFile markdownFile) throws IOException {
        if (!image.isEmpty()) {
            course.setImageUrl(fileStorageService.storeFile(image));
        }
        if (markdownFile != null && !markdownFile.isEmpty()) {
            course.setContent(new String(markdownFile.getBytes(), StandardCharsets.UTF_8));
        }
        courseService.updateCourse(id, course);
        return "redirect:/admin/courses";
    }

    /**
     * Удаление курса.
     */
    @GetMapping("/delete-course/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/admin/courses";
    }

    /**
     * Управление модулями курса.
     */
    @GetMapping("/courses/{id}/modules")
    public String manageModules(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        model.addAttribute("course", course);
        model.addAttribute("modules", courseService.getModulesByCourseId(id));
        model.addAttribute("title", "Manage Modules: " + course.getTitle());
        return "admin/modules";
    }

    /**
     * Добавление модуля.
     */
    @PostMapping("/courses/{id}/modules")
    public String addModule(@PathVariable Long id,
                            @RequestParam("title") String title,
                            @RequestParam(value = "content", required = false) String content,
                            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        CourseModule module = new CourseModule();
        module.setTitle(title);
        
        if (file != null && !file.isEmpty()) {
            module.setContent(new String(file.getBytes(), StandardCharsets.UTF_8));
        } else {
            module.setContent(content);
        }
        
        courseService.addModule(id, module);
        return "redirect:/admin/courses/" + id + "/modules";
    }

    /**
     * Удаление модуля.
     */
    @GetMapping("/delete-module/{courseId}/{moduleId}")
    public String deleteModule(@PathVariable Long courseId, @PathVariable Long moduleId) {
        courseService.deleteModule(moduleId);
        return "redirect:/admin/courses/" + courseId + "/modules";
    }
}
