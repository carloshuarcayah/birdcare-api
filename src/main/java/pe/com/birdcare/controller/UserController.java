package pe.com.birdcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.birdcare.dto.UserResponseDTO;
import pe.com.birdcare.service.IUserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAll(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<UserResponseDTO>> getJustActives(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.findJustActives(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseDTO>> getByName(
            @RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.findByName(pageable, name));
    }
}
