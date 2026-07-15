package com.example.template.quarkus.dto.response;

import com.example.template.quarkus.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private boolean active;
    private Set<String> roles;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .active(user.isActive())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
