package com.example.template.quarkus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Konfigurasi Jackson global:
 * - snake_case untuk semua field (menggantikan @JsonNaming di setiap DTO)
 * - Format LocalDateTime: "yyyy-MM-dd HH:mm:ss"
 *
 * Menggantikan Spring Boot's JacksonConfig dengan ObjectMapperCustomizer (Quarkus CDI).
 */
@Singleton
public class JacksonConfig implements ObjectMapperCustomizer {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void customize(ObjectMapper mapper) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }
}
