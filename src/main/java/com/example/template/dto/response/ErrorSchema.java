package com.example.template.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorSchema {

    private String errorCode;
    private ErrorMessage errorMessage;

    public static ErrorSchema success() {
        return ErrorSchema.builder()
                .errorCode("RST-000")
                .errorMessage(ErrorMessage.builder().english("Success").indonesian("Berhasil").build())
                .build();
    }

    public static ErrorSchema validationError(String msgEn, String msgId) {
        return ErrorSchema.builder()
                .errorCode("RST-001")
                .errorMessage(ErrorMessage.builder().english(msgEn).indonesian(msgId).build())
                .build();
    }

    public static ErrorSchema notFound(String msgEn, String msgId) {
        return ErrorSchema.builder()
                .errorCode("RST-002")
                .errorMessage(ErrorMessage.builder().english(msgEn).indonesian(msgId).build())
                .build();
    }

    public static ErrorSchema businessError(String msgEn, String msgId) {
        return ErrorSchema.builder()
                .errorCode("RST-003")
                .errorMessage(ErrorMessage.builder().english(msgEn).indonesian(msgId).build())
                .build();
    }

    public static ErrorSchema unauthorized(String msgEn, String msgId) {
        return ErrorSchema.builder()
                .errorCode("RST-401")
                .errorMessage(ErrorMessage.builder().english(msgEn).indonesian(msgId).build())
                .build();
    }

    public static ErrorSchema forbidden(String msgEn, String msgId) {
        return ErrorSchema.builder()
                .errorCode("RST-403")
                .errorMessage(ErrorMessage.builder().english(msgEn).indonesian(msgId).build())
                .build();
    }

    public static ErrorSchema internalError(String msgEn, String msgId) {
        return ErrorSchema.builder()
                .errorCode("RST-500")
                .errorMessage(ErrorMessage.builder().english(msgEn).indonesian(msgId).build())
                .build();
    }
}
