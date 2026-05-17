package com.example.blog.service;

import com.example.blog.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    void updateProfile(String username, String fullName, String phoneNumber, MultipartFile avatar);
}
