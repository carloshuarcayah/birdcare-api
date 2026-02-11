package pe.com.birdcare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//id - IS AUTOGENERATE
//email - NOT NULL
//pasword - NOT NULL
//name - NOT NULL
//last name - NOT NULL
//phone - NULLABLE
//role - HAS A DEFAULT CUSTOMER - JUST THE ADMIN WILL HAVE THE OPTION OF MODIFIDE THE ROL
//active - HAS A DEFAULT TRUE
public record UserUpdateDTO(
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,
        @NotBlank(message = "name is required")
        String name,
        @NotBlank(message = "lastname is required")
        String lastName,
        @Size(max = 15)
        String phone
) {
}
