package pe.com.birdcare.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponseDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        Boolean active,
        String categoryName
) {
}
