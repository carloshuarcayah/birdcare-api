package pe.com.birdcare.dto;
import jakarta.validation.constraints.*;
import pe.com.birdcare.enums.Role;

public record UserRequestDTO(
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be between 8 and 30 characters")
        String password,

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "lastname is required")
        String lastName,

        @Size(max = 15) String phone,

        @NotNull Role role
){
}
