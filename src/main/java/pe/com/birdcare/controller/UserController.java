package pe.com.birdcare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.com.birdcare.dto.UserCreateDTO;
import pe.com.birdcare.dto.UserPasswordChangeDTO;
import pe.com.birdcare.dto.UserResponseDTO;
import pe.com.birdcare.dto.UserUpdateDTO;
import pe.com.birdcare.service.IUserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    //CUSTOMER - ADMIN
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUser(@AuthenticationPrincipal UserDetails auth){
        return ResponseEntity.ok(userService.findMe(auth.getUsername()));
    }

    //USER
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> update(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody UserUpdateDTO request) {
        return ResponseEntity.ok(userService.update(request, user.getUsername()));
    }

    //CUSTOMER
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal UserDetails auth, @Valid @RequestBody UserPasswordChangeDTO req) {
        userService.changePassword(auth.getUsername(), req);
    }

}