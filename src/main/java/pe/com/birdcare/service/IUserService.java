package pe.com.birdcare.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pe.com.birdcare.dto.*;

public interface IUserService {
    Page<UserResponseDTO> findAll(Pageable pageable);
    Page<UserResponseDTO> findActiveUsers(Pageable pageable);
    UserResponseDTO findById(Long id);
    UserResponseDTO findMe(String email);
    Page<UserResponseDTO> findByName(Pageable pageable,String name);
    UserResponseDTO create(UserCreateDTO obj);
    UserResponseDTO update(UserUpdateDTO obj, String email);
    void disable(Long id);
    void enable(Long id);

    void resetPassword(Long id, AdminPasswordResetDTO req);
    void changePassword(String email, UserPasswordChangeDTO req);
}
