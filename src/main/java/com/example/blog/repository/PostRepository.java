package com.example.blog.repository;

import com.example.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Поиск постов по ключевому слову в заголовке или тексте с поддержкой пагинации.
     * Аналог логики поиска из Node.js версии.
     */
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.body) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Стандартный метод findAll(Pageable) уже доступен в JpaRepository для пагинации главной страницы.
     */
}
