package com.example.template.quarkus.dto.response;

import com.example.template.quarkus.entity.Tag;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagResponse {

    private Long id;
    private String name;

    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
