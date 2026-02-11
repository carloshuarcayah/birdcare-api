package pe.com.birdcare.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pe.com.birdcare.dto.UserPasswordChangeDTO;
import pe.com.birdcare.dto.UserCreateDTO;
import pe.com.birdcare.dto.UserResponseDTO;
import pe.com.birdcare.dto.UserUpdateDTO;

public interface IUserService {
    Page<UserResponseDTO> findAll(Pageable pageable);
    Page<UserResponseDTO> findJustActives(Pageable pageable);
    UserResponseDTO findById(Long id);
    Page<UserResponseDTO> findByName(Pageable pageable,String name);
    UserResponseDTO create(UserCreateDTO obj);
    UserResponseDTO update(UserUpdateDTO obj, Long id);
    void delete(Long id);
    void enable(Long id);
    void changePassword(Long id, UserPasswordChangeDTO req);
}
