package pe.com.birdcare.dto;

import lombok.Builder;

@Builder
public record CategoryResponseDTO(
        Long id,
        String name,
        String description,
        Boolean active
) {
}
