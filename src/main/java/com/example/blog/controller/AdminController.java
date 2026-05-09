package com.example.blog.controller;

import com.example.blog.entity.Post;
import com.example.blog.service.FileStorageService;
import com.example.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PostService postService;
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
                          @RequestParam(value = "tagIds", required = false) List<Long> tagIds) {
        if (!image.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(image);
            post.setImageUrl(imageUrl);
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
                             @RequestParam(value = "tagIds", required = false) List<Long> tagIds) {
        if (!image.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(image);
            post.setImageUrl(imageUrl);
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
}
