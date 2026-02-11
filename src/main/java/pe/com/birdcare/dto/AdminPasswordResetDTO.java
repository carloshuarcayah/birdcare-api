package pe.com.birdcare.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminPasswordResetDTO(
        @NotBlank(message = "New password is required")
        @Size(min = 8,max = 30,message = "New password must be between 8 and 30 characters")
        String newPassword
) {}
