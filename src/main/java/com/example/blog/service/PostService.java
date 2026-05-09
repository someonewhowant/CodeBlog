package com.example.blog.service;

import com.example.blog.entity.Post;
import org.springframework.data.domain.Page;
import java.util.List;

public interface PostService {
    Page<Post> getAllPosts(int page);
    List<Post> getAllPosts();
    Post getPostById(Long id);
    Page<Post> searchPosts(String query, int page);
    Post createPost(Post post);
    Post updatePost(Long id, Post postDetails);
    void deletePost(Long id);
}
