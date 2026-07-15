package com.example.template.quarkus.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Nama produk tidak boleh kosong")
    private String name;

    private String description;

    @NotNull(message = "Harga tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Harga harus lebih dari 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stok tidak boleh negatif")
    private int stock;

    private LocalDateTime publishedAt;

    @NotNull(message = "Category ID tidak boleh kosong")
    private Long categoryId;

    private Set<Long> tagIds = new HashSet<>();
}
