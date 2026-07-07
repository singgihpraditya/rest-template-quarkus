package com.example.template.service;

import com.example.template.dto.response.PostResponse;
import com.example.template.rest.JsonPlaceholderClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
@Slf4j
public class ExternalApiService {

    @Inject
    @RestClient
    JsonPlaceholderClient jsonPlaceholderClient;

    public List<PostResponse> getAllPosts() {
        log.debug("Fetching all posts from JSONPlaceholder");
        return jsonPlaceholderClient.getAllPosts();
    }

    public PostResponse getPostById(Long id) {
        log.debug("Fetching post {} from JSONPlaceholder", id);
        return jsonPlaceholderClient.getPostById(id);
    }

    public List<PostResponse> getPostsByUserId(Long userId) {
        log.debug("Fetching posts for user {} from JSONPlaceholder", userId);
        return jsonPlaceholderClient.getPostsByUserId(userId);
    }
}
