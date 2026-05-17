package com.example.blog.controller;

import com.example.blog.service.PostService;
import com.example.blog.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final PostService postService;
    private final UserService userService;

    @ModelAttribute("categories")
    public Object categories() {
        return postService.getAllCategories();
    }

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("currentUser")
    public Object currentUser(Principal principal) {
        if (principal == null) return null;
        return userService.findByUsername(principal.getName()).orElse(null);
    }
}
