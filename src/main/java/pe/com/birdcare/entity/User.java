package pe.com.birdcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import pe.com.birdcare.enums.Role;

import java.io.Serializable;


@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Column(nullable = false,length = 100)
    @NotBlank(message = "name is required")
    private String name;

    @Column(name = "last_name",nullable = false,length = 100)
    @NotBlank(message = "lastname is required")
    private String lastName;

    @Column(nullable = true,length = 15)
    private String phone;

    //USER ROL IS DETERMINATE FOR THE
    @Enumerated(EnumType.STRING)
    @Column(length = 20,nullable = false)
    @Builder.Default
    private Role role=Role.CUSTOMER;

    @Column(nullable = false)
    @Builder.Default
    @NotNull
    private Boolean active = true;

}
