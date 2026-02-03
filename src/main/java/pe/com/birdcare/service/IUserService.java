package pe.com.birdcare.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pe.com.birdcare.dto.UserRequestDTO;
import pe.com.birdcare.dto.UserResponseDTO;

public interface IUserService {
    Page<UserResponseDTO> findAll(Pageable pageable);
    Page<UserResponseDTO> findJustActives(Pageable pageable);
    UserResponseDTO findById(Long id);
    Page<UserResponseDTO> findByName(Pageable pageable,String name);
    UserResponseDTO add(UserRequestDTO obj);
    UserResponseDTO update(UserRequestDTO obj, Long id);
    void delete(Long id);
}
