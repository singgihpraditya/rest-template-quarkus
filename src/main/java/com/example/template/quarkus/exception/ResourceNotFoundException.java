package com.example.template.quarkus.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " dengan ID " + id + " tidak ditemukan");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
