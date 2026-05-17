package com.example.blog.controller;

import com.example.blog.entity.User;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("message", "Вы вошли как Преподаватель");
        model.addAttribute("title", "Teacher Cabinet");
        return "teacher/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("title", "Настройки профиля");
        return "teacher/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Principal principal) {
        
        userService.updateProfile(principal.getName(), fullName, phoneNumber, avatar);
        return "redirect:/teacher/profile?success";
    }
}
