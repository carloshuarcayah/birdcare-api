package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.birdcare.dto.UserRequestDTO;
import pe.com.birdcare.dto.UserResponseDTO;
import pe.com.birdcare.entity.User;
import pe.com.birdcare.mapper.UserMapper;
import pe.com.birdcare.repository.UserRepository;
import pe.com.birdcare.service.IUserService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponseDTO);
    }

    @Override
    public Page<UserResponseDTO> findJustActives(Pageable pageable) {
        return userRepository.findAllByActiveTrue(pageable).map(userMapper::toResponseDTO);
    }

    @Override
    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id).map(userMapper::toResponseDTO)
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public Page<UserResponseDTO> findByName(Pageable pageable, String name) {
        return userRepository.findAllByNameContainingIgnoreCase(pageable, name)
                .map(userMapper::toResponseDTO);
    }


    @Transactional
    @Override
    public UserResponseDTO add(UserRequestDTO obj) {
        User user = userMapper.fromRequestToEntity(obj);
        String encryptedPassword = passwordEncoder.encode(obj.password());
        user.setPassword(encryptedPassword);

        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponseDTO update(UserRequestDTO obj, Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(RuntimeException::new);

        userMapper.updateEntityFromDto(obj,existingUser);
        String encryptedPassword = "Encrypted_"+obj.password();
        existingUser.setPassword(encryptedPassword);

        return userMapper.toResponseDTO(userRepository.save(existingUser));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        User existingUser = userRepository.findById(id).orElseThrow(RuntimeException::new);

        if(existingUser.getActive()){
            existingUser.setActive(false);
            userRepository.save(existingUser);
        }
    }

    @Transactional
    @Override
    public void enable(Long id) {
        User existingUser = userRepository.findById(id).orElseThrow(RuntimeException::new);

        if(!existingUser.getActive()){
            existingUser.setActive(true);
            userRepository.save(existingUser);
        }
    }
}
