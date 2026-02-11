package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.birdcare.dto.*;
import pe.com.birdcare.entity.User;
import pe.com.birdcare.exception.ResourceNotFoundException;
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
    public Page<UserResponseDTO> findActiveUsers(Pageable pageable) {
        return userRepository.findAllByActiveTrue(pageable).map(userMapper::response);
    }

    @Override
    public UserResponseDTO findById(Long id) {
        User existingUser = getUserOrThrow(id);

        return userMapper.response(existingUser);
    }

    @Override
    public UserResponseDTO findMe(String email) {
        User user = getUserByEmailOrThrow(email);
        return userMapper.response(user);
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
    public UserResponseDTO update(UserUpdateDTO obj, String email) {
        User existingUser = getUserByEmailOrThrow(email);
        userMapper.updateUser(obj,existingUser);
        return userMapper.response(userRepository.save(existingUser));
    }

    @Transactional
    @Override
    public void disable(Long id) {
        User existingUser = getUserOrThrow(id);
        existingUser.setActive(false);
        userRepository.save(existingUser);
    }

    @Transactional
    @Override
    public void enable(Long id) {
        User existingUser = getUserOrThrow(id);
        existingUser.setActive(true);
        userRepository.save(existingUser);
    }

    //ADMIN
    @Transactional
    @Override
    public void resetPassword(Long id, AdminPasswordResetDTO req) {
        User existingUser = getUserOrThrow(id);

        String encodedPassword = passwordEncoder.encode(req.newPassword());

        existingUser.setPassword(encodedPassword);

        userRepository.save(existingUser);
    }

    //user
    @Transactional
    @Override
    public void changePassword(String email, UserPasswordChangeDTO req) {
        User existingUser = getUserByEmailOrThrow(email);

        if(!passwordEncoder.matches(req.oldPassword(), existingUser.getPassword()))
            throw new BadCredentialsException("Invalid credentials");

        String encodedPassword = passwordEncoder.encode(req.newPassword());

        existingUser.setPassword(encodedPassword);

        userRepository.save(existingUser);
    }

    private User getUserOrThrow(Long id){
        return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with id: "+id));
    }

    private User getUserByEmailOrThrow(String email){
        return  userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found with email: "+email));
    }
}
