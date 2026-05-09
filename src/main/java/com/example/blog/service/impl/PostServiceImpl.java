package com.example.blog.service.impl;

import com.example.blog.entity.Post;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.repository.PostRepository;
import com.example.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private static final int PAGE_SIZE = 6;

    @Override
    public Page<Post> getAllPosts(int page) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, PAGE_SIZE, Sort.by("createdAt").descending());
        return postRepository.findAll(pageable);
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll(Sort.by("createdAt").descending());
    }

    @Override
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    @Override
    public Page<Post> searchPosts(String query, int page) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, PAGE_SIZE, Sort.by("createdAt").descending());
        return postRepository.searchByKeyword(query, pageable);
    }

    @Override
    @Transactional
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post updatePost(Long id, Post postDetails) {
        Post post = getPostById(id);
        
        post.setTitle(postDetails.getTitle());
        post.setBody(postDetails.getBody());
        if (postDetails.getImageUrl() != null) {
            post.setImageUrl(postDetails.getImageUrl());
        }
        
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        Post post = getPostById(id);
        postRepository.delete(post);
    }
}
