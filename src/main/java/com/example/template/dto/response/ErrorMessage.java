package com.example.template.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorMessage {
    private String english;
    private String indonesian;
}
