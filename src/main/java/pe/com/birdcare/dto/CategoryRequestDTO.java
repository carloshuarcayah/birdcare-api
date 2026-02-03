package pe.com.birdcare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public record CategoryRequestDTO(
        @NotBlank String name,
        @NotBlank String description
) {
}
