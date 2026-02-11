package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.birdcare.dto.UserPasswordChangeDTO;
import pe.com.birdcare.dto.UserCreateDTO;
import pe.com.birdcare.dto.UserResponseDTO;
import pe.com.birdcare.dto.UserUpdateDTO;
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
        return userRepository.findAll(pageable).map(userMapper::response);
    }

    @Override
    public Page<UserResponseDTO> findJustActives(Pageable pageable) {
        return userRepository.findAllByActiveTrue(pageable).map(userMapper::response);
    }

    @Override
    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id).map(userMapper::response)
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public Page<UserResponseDTO> findByName(Pageable pageable, String name) {
        return userRepository.findAllByNameContainingIgnoreCase(pageable, name)
                .map(userMapper::response);
    }


    @Transactional
    @Override
    public UserResponseDTO create(UserCreateDTO obj) {
        User user = userMapper.createUser(obj);
        String encryptedPassword = passwordEncoder.encode(obj.password());
        user.setPassword(encryptedPassword);

        return userMapper.response(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponseDTO update(UserUpdateDTO obj, Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(RuntimeException::new);

        //password do not change
        userMapper.updateUser(obj,existingUser);

        return userMapper.response(userRepository.save(existingUser));
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

    @Transactional
    @Override
    public void changePassword(Long id, UserPasswordChangeDTO req) {
        User existingUser = userRepository.findById(id).orElseThrow(RuntimeException::new);

        if(!passwordEncoder.matches(req.oldPassword(), existingUser.getPassword()))
            throw new BadCredentialsException("Invalid credentials");

        String encodedPassword = passwordEncoder.encode(req.newPassword());

        existingUser.setPassword(encodedPassword);

        userRepository.save(existingUser);
    }
}
