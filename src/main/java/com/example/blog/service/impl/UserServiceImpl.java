package com.example.blog.service.impl;

import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.FileStorageService;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public void updateProfile(String username, String fullName, String phoneNumber, MultipartFile avatar) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = fileStorageService.storeFile(avatar);
            user.setAvatarUrl(avatarUrl);
        }

        userRepository.save(user);
    }
}
