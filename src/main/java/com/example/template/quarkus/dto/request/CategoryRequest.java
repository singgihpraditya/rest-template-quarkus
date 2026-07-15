package com.example.template.quarkus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Nama kategori tidak boleh kosong")
    @Size(max = 100, message = "Nama kategori maks 100 karakter")
    private String name;

    @Size(max = 500, message = "Deskripsi maks 500 karakter")
    private String description;
}
