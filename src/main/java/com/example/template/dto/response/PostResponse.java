package com.example.template.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO untuk response dari API eksternal JSONPlaceholder.
 * Menggunakan @JsonProperty eksplisit karena API eksternal mengembalikan camelCase,
 * sementara konvensi internal project ini adalah snake_case via JacksonConfig.
 */
@Getter
@NoArgsConstructor
public class PostResponse {

    private Long id;

    @JsonProperty("userId")
    private Long userId;

    private String title;
    private String body;
}
