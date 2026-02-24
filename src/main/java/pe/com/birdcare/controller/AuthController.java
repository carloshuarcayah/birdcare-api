package pe.com.birdcare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.birdcare.dto.LoginRequestDTO;
import pe.com.birdcare.dto.LoginResponseDTO;
import pe.com.birdcare.dto.UserCreateDTO;
import pe.com.birdcare.dto.UserResponseDTO;
import pe.com.birdcare.security.JwtUtil;
import pe.com.birdcare.service.IUserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IUserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserCreateDTO request) {
        return new ResponseEntity<>(userService.create(request), HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO req) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        String jwt = jwtUtil.generateToken(auth.getName());

        UserDetails user = (UserDetails) auth.getPrincipal();

        List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        return ResponseEntity.ok(new LoginResponseDTO(jwt,user.getUsername(),roles));
    }
}