package com.example.blog;

import com.example.blog.entity.Post;
import com.example.blog.entity.User;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(PostRepository postRepository, 
                               UserRepository userRepository, 
                               PasswordEncoder passwordEncoder) {
        return args -> {
            // Создаем админа, если его нет
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin")) // Пароль: admin
                        .build();
                userRepository.save(admin);
            }

            // Добавляем тестовые посты, если база пуста
            if (postRepository.count() == 0) {
                postRepository.save(Post.builder()
                        .title("Welcome to Spring Blog")
                        .body("This is your first post migrated from Node.js to Spring Boot. Enjoy!")
                        .build());
                
                postRepository.save(Post.builder()
                        .title("Spring Boot vs Node.js")
                        .body("Spring Boot provides a robust ecosystem and strong typing, making it great for enterprise apps.")
                        .build());

                postRepository.save(Post.builder()
                        .title("Thymeleaf Layouts")
                        .body("Using layouts in Thymeleaf makes your frontend clean and modular.")
                        .build());
            }
        };
    }
}
