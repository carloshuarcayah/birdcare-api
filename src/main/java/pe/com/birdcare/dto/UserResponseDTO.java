package pe.com.birdcare.dto;


import lombok.Builder;
import pe.com.birdcare.enums.Role;

@Builder
public record UserResponseDTO(
        Long id,
        String email,
        String name,
        String lastName,
        String phone,
        String role,
        Boolean active
) {
}
