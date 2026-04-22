package pe.com.birdcare.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.birdcare.dto.*;
import pe.com.birdcare.entity.User;
import pe.com.birdcare.exception.ConflictException;
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
        return userMapper.response(getUserOrThrow(id));
    }

    @Override
    public UserResponseDTO findMe(String email) {
        return userMapper.response(getUserByEmailOrThrow(email));
    }

    @Override
    public Page<UserResponseDTO> findByName(Pageable pageable, String name) {
        return userRepository.findAllByNameContainingIgnoreCase(pageable, name)
                .map(userMapper::response);
    }

    @Transactional
    @Override
    public UserResponseDTO create(UserCreateDTO obj) {
        if (userRepository.existsByEmail(obj.email())) {
            throw new ConflictException("Email already registered: " + obj.email());
        }
        String encoded = passwordEncoder.encode(obj.password());
        User user = User.registerCustomer(obj.email(), encoded, obj.name(), obj.lastName(), obj.phone());
        return userMapper.response(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponseDTO update(UserUpdateDTO obj, String email) {
        User existingUser = getUserByEmailOrThrow(email);
        existingUser.updateInfo(obj.name(), obj.lastName(), obj.phone());
        return userMapper.response(userRepository.save(existingUser));
    }

    @Transactional
    @Override
    public void disable(Long id) {
        User existingUser = getUserOrThrow(id);
        existingUser.disable();
        userRepository.save(existingUser);
    }

    @Transactional
    @Override
    public void enable(Long id) {
        User existingUser = getUserOrThrow(id);
        existingUser.enable();
        userRepository.save(existingUser);
    }

    @Transactional
    @Override
    public void resetPassword(Long id, AdminPasswordResetDTO req) {
        User existingUser = getUserOrThrow(id);
        existingUser.resetPassword(req.newPassword(), passwordEncoder);
        userRepository.save(existingUser);
    }

    @Transactional
    @Override
    public void changePassword(String email, UserPasswordChangeDTO req) {
        User existingUser = getUserByEmailOrThrow(email);
        existingUser.changePassword(req.oldPassword(), req.newPassword(), passwordEncoder);
        userRepository.save(existingUser);
    }

    private User getUserOrThrow(Long id){
        return userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with id: "+id));
    }

    private User getUserByEmailOrThrow(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with email: "+email));
    }
}