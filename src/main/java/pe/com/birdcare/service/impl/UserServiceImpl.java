package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.com.birdcare.dto.UserRequestDTO;
import pe.com.birdcare.dto.UserResponseDTO;
import pe.com.birdcare.entity.User;
import pe.com.birdcare.repository.UserRepository;
import pe.com.birdcare.service.IUserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;

    @Override
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public Page<UserResponseDTO> findJustActives(Pageable pageable) {
        return userRepository.findAllByActiveTrue(pageable).map(this::toDTO);
    }

    @Override
    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id).map(this::toDTO)
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public Page<UserResponseDTO> findByName(Pageable pageable, String name) {
        return userRepository.findAllByNameContainingIgnoreCase(pageable, name)
                .map(this::toDTO);
    }

    @Override
    public UserResponseDTO add(UserRequestDTO obj) {
        User user = User.builder()
                .email(obj.email())
                .password(obj.password())
                .name(obj.name())
                .lastName(obj.lastName())
                .phone(obj.phone())
                .role(obj.role())
                .build();

        return null;
    }

    @Override
    public UserResponseDTO update(UserRequestDTO obj, Long id) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    public UserResponseDTO toDTO(User user){
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().getValue())
                .active(user.getActive())
                .build();
    }
}
