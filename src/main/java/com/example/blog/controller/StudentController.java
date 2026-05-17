package com.example.blog.controller;

import com.example.blog.entity.CourseEnrollment;
import com.example.blog.entity.User;
import com.example.blog.repository.CourseEnrollmentRepository;
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
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final UserService userService;
    private final CourseEnrollmentRepository enrollmentRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        
        var activeEnrollments = enrollmentRepository.findByUserAndStatus(user, CourseEnrollment.EnrollmentStatus.IN_PROGRESS);
        var completedEnrollments = enrollmentRepository.findByUserAndStatus(user, CourseEnrollment.EnrollmentStatus.COMPLETED);
        
        model.addAttribute("user", user);
        model.addAttribute("activeCourses", activeEnrollments);
        model.addAttribute("completedCourses", completedEnrollments);
        model.addAttribute("message", "Вы вошли как Студент");
        model.addAttribute("title", "Student Cabinet");
        return "student/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("title", "Настройки профиля");
        return "student/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Principal principal) {
        
        userService.updateProfile(principal.getName(), fullName, phoneNumber, avatar);
        return "redirect:/student/profile?success";
    }
}
