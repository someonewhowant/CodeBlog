package com.example.blog.controller;

import com.example.blog.entity.Post;
import com.example.blog.service.MarkdownService;
import com.example.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PostService postService;
    private final MarkdownService markdownService;

    /**
     * Главная страница блога с пагинацией.
     */
    @GetMapping("")
    public String index(@RequestParam(defaultValue = "1") int page, Model model) {
        Page<Post> postPage = postService.getAllPosts(page);
        
        List<Post> posts = postPage.getContent().stream().map(post -> 
            Post.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(markdownService.convertToHtml(post.getBody()).replaceAll("<[^>]*>", ""))
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .build()
        ).collect(Collectors.toList());

        model.addAttribute("data", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("title", "CodeBlog");
        model.addAttribute("description", "A modern blog platform built with Spring Boot 3.");
        
        return "index";
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
                .build();

        model.addAttribute("post", displayPost);
        model.addAttribute("title", post.getTitle());
        
        return "post";
    }

    /**
     * Поиск постов по ключевому слову.
     */
    @GetMapping("/search")
    public String search(@RequestParam String searchTerm, 
                         @RequestParam(defaultValue = "1") int page, 
                         Model model) {
        Page<Post> searchResults = postService.searchPosts(searchTerm, page);
        
        List<Post> posts = searchResults.getContent().stream().map(post -> 
            Post.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(markdownService.convertToHtml(post.getBody()).replaceAll("<[^>]*>", ""))
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .build()
        ).collect(Collectors.toList());

        model.addAttribute("data", posts);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("title", "Search Results");
        
        return "search";
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
