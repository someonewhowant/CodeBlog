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
     * Статическая страница "About".
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About Us");
        return "about";
    }
}
