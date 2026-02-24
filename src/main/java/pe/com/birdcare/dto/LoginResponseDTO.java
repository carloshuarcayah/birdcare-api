package pe.com.birdcare.dto;

import java.util.List;

public record LoginResponseDTO(
        String token,
        String email,
        List<String> roles
) {
}
