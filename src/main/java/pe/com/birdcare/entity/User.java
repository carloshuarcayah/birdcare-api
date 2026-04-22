package pe.com.birdcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.com.birdcare.enums.Role;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false,length = 100)
    private String name;

    @Column(name = "last_name",nullable = false,length = 100)
    private String lastName;

    @Column(length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20,nullable = false)
    private Role role;

    @Column(nullable = false)
    private Boolean active;

    @Builder
    private User(String email, String password, String name, String lastName, String phone, Role role, Boolean active) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
        this.active = active;
    }

    public static User registerCustomer(String email, String password, String name, String lastName, String phone){
        validateField(name,"Name");
        validateField(lastName,"Last name");
        validateField(email,"email");
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .lastName(lastName)
                .role(Role.CUSTOMER)
                .phone(phone)
                .active(true)
                .build();
    }



    public static User createAdmin(String email, String password, String name, String lastName, String phone){
        validateField(name,"Name");
        validateField(lastName,"Last name");
        validateField(email,"email");
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .lastName(lastName)
                .role(Role.ADMIN)
                .phone(phone)
                .active(true)
                .build();
    }

    public void enable(){
        this.active=true;
    }

    public void disable(){
        this.active=false;
    }

    public void updateInfo(String name, String lastName, String phone){
        validateField(name,"Name");
        validateField(lastName,"Last name");
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
    }

    public void changePassword(String currentPass, String newPass, PasswordEncoder encoder){

        if(currentPass==null||!encoder.matches(currentPass,this.password)){
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if(currentPass.equals(newPass)){
            throw new IllegalArgumentException("New password cannot be the same as the current one");
        }

        password=encoder.encode(newPass);
    }

    public void resetPassword(String newPass, PasswordEncoder encoder){
        validateField(newPass, "New password");
        this.password = encoder.encode(newPass);
    }

    private static void validateField(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }
}
