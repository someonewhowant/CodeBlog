package com.example.blog.config;

import com.example.blog.entity.Category;
import com.example.blog.entity.Post;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(CategoryRepository categoryRepository, 
                                      TagRepository tagRepository,
                                      PostRepository postRepository,
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Инициализация пользователя
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin")) // Пароль: admin
                        .build();
                userRepository.save(admin);
            }

            // Инициализация категорий
            if (categoryRepository.count() == 0) {
                categoryRepository.saveAll(Arrays.asList(
                    Category.builder().name("Backend").slug("backend").build(),
                    Category.builder().name("Frontend").slug("frontend").build(),
                    Category.builder().name("DevOps").slug("devops").build(),
                    Category.builder().name("Architecture").slug("architecture").build()
                ));
            }

            // Инициализация тегов
            if (tagRepository.count() == 0) {
                tagRepository.saveAll(Arrays.asList(
                    Tag.builder().name("Java").slug("java").build(),
                    Tag.builder().name("Spring Boot").slug("spring-boot").build(),
                    Tag.builder().name("Docker").slug("docker").build(),
                    Tag.builder().name("Security").slug("security").build(),
                    Tag.builder().name("Microservices").slug("microservices").build(),
                    Tag.builder().name("TypeScript").slug("typescript").build()
                ));
            }

            // Инициализация тестовых постов
            if (postRepository.count() == 0) {
                Category backend = categoryRepository.findBySlug("backend").orElse(null);
                
                postRepository.save(Post.builder()
                        .title("Welcome to Spring Blog")
                        .body("This is your first post migrated from Node.js to Spring Boot. Enjoy!")
                        .category(backend)
                        .build());
                
                postRepository.save(Post.builder()
                        .title("Spring Boot vs Node.js")
                        .body("Spring Boot provides a robust ecosystem and strong typing, making it great for enterprise apps.")
                        .category(backend)
                        .build());

                postRepository.save(Post.builder()
                        .title("Thymeleaf Layouts")
                        .body("Using layouts in Thymeleaf makes your frontend clean and modular.")
                        .category(categoryRepository.findBySlug("frontend").orElse(null))
                        .build());
            }
        };
    }
}
