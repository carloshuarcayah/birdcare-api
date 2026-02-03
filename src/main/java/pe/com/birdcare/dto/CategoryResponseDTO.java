package pe.com.birdcare.dto;

import lombok.Builder;

@Builder
public record CategoryResponseDTO(
        Long id,
        String nombre,
        String description,
        Boolean active
) {
}
