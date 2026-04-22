package pe.com.birdcare.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.com.birdcare.dto.*;
import pe.com.birdcare.entity.User;
import pe.com.birdcare.enums.Role;
import pe.com.birdcare.exception.ConflictException;
import pe.com.birdcare.exception.ResourceNotFoundException;
import pe.com.birdcare.mapper.UserMapper;
import pe.com.birdcare.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "carlos@test.com";
    private static final String RAW_PASSWORD = "Secret123";

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl service;

    private User existingUser;
    private UserResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        existingUser = User.registerCustomer(EMAIL, "enc:" + RAW_PASSWORD, "Carlos", "Huarcaya", "999");
        responseDto = new UserResponseDTO(USER_ID, EMAIL, "Carlos", "Huarcaya", "999", "CUSTOMER", true);
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        void returnsDtoWhenFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
            when(userMapper.response(existingUser)).thenReturn(responseDto);

            UserResponseDTO result = service.findById(USER_ID);

            assertThat(result).isEqualTo(responseDto);
        }

        @Test
        void throwsWhenNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(USER_ID.toString());

            verify(userMapper, never()).response(any());
        }
    }

    @Nested
    @DisplayName("findMe")
    class FindMe {

        @Test
        void returnsDtoWhenFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
            when(userMapper.response(existingUser)).thenReturn(responseDto);

            assertThat(service.findMe(EMAIL)).isEqualTo(responseDto);
        }

        @Test
        void throwsWhenNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findMe(EMAIL))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(EMAIL);
        }
    }

    @Nested
    @DisplayName("findAll / findActiveUsers / findByName")
    class PagedQueries {

        private final Pageable pageable = PageRequest.of(0, 10);

        @Test
        void findAllMapsEachEntity() {
            Page<User> page = new PageImpl<>(List.of(existingUser));
            when(userRepository.findAll(pageable)).thenReturn(page);
            when(userMapper.response(existingUser)).thenReturn(responseDto);

            Page<UserResponseDTO> result = service.findAll(pageable);

            assertThat(result.getContent()).containsExactly(responseDto);
        }

        @Test
        void findActiveUsersDelegatesToActiveQuery() {
            when(userRepository.findAllByActiveTrue(pageable))
                    .thenReturn(new PageImpl<>(List.of(existingUser)));
            when(userMapper.response(existingUser)).thenReturn(responseDto);

            service.findActiveUsers(pageable);

            verify(userRepository).findAllByActiveTrue(pageable);
            verify(userRepository, never()).findAll(pageable);
        }

        @Test
        void findByNameDelegatesToNameQuery() {
            when(userRepository.findAllByNameContainingIgnoreCase(pageable, "carlos"))
                    .thenReturn(new PageImpl<>(List.of(existingUser)));
            when(userMapper.response(existingUser)).thenReturn(responseDto);

            service.findByName(pageable, "carlos");

            verify(userRepository).findAllByNameContainingIgnoreCase(pageable, "carlos");
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        private final UserCreateDTO dto =
                new UserCreateDTO(EMAIL, RAW_PASSWORD, "Carlos", "Huarcaya", "999");

        @Test
        void encodesPasswordAndSavesCustomer() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn("enc:" + RAW_PASSWORD);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.response(any(User.class))).thenReturn(responseDto);

            UserResponseDTO result = service.create(dto);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();

            assertThat(saved.getEmail()).isEqualTo(EMAIL);
            assertThat(saved.getPassword()).isEqualTo("enc:" + RAW_PASSWORD);
            assertThat(saved.getRole()).isEqualTo(Role.CUSTOMER);
            assertThat(saved.getActive()).isTrue();
            assertThat(result).isEqualTo(responseDto);
        }

        @Test
        void throwsConflictWhenEmailExists() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(EMAIL);

            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        private final UserUpdateDTO dto =
                new UserUpdateDTO(EMAIL, "Pedro", "Perez", "111");

        @Test
        void updatesDomainFieldsAndSaves() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(existingUser)).thenReturn(existingUser);
            when(userMapper.response(existingUser)).thenReturn(responseDto);

            service.update(dto, EMAIL);

            assertThat(existingUser.getName()).isEqualTo("Pedro");
            assertThat(existingUser.getLastName()).isEqualTo("Perez");
            assertThat(existingUser.getPhone()).isEqualTo("111");
            verify(userRepository).save(existingUser);
        }

        @Test
        void throwsWhenNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(dto, EMAIL))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("disable / enable")
    class DisableEnable {

        @Test
        void disableTurnsActiveFalseAndSaves() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

            service.disable(USER_ID);

            assertThat(existingUser.getActive()).isFalse();
            verify(userRepository).save(existingUser);
        }

        @Test
        void enableTurnsActiveTrueAndSaves() {
            existingUser.disable();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

            service.enable(USER_ID);

            assertThat(existingUser.getActive()).isTrue();
            verify(userRepository).save(existingUser);
        }

        @Test
        void disableThrowsWhenNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.disable(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        private final AdminPasswordResetDTO dto = new AdminPasswordResetDTO("BrandNew1");

        @Test
        void encodesAndSaves() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.encode("BrandNew1")).thenReturn("enc:BrandNew1");

            service.resetPassword(USER_ID, dto);

            assertThat(existingUser.getPassword()).isEqualTo("enc:BrandNew1");
            verify(userRepository).save(existingUser);
        }

        @Test
        void throwsWhenNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.resetPassword(USER_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        private final UserPasswordChangeDTO dto =
                new UserPasswordChangeDTO(RAW_PASSWORD, "BrandNew1");

        @Test
        void encodesAndSavesWhenCurrentMatches() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(RAW_PASSWORD, "enc:" + RAW_PASSWORD)).thenReturn(true);
            when(passwordEncoder.encode("BrandNew1")).thenReturn("enc:BrandNew1");

            service.changePassword(EMAIL, dto);

            assertThat(existingUser.getPassword()).isEqualTo("enc:BrandNew1");
            verify(userRepository).save(existingUser);
        }

        @Test
        void propagatesDomainErrorWhenCurrentWrong() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(RAW_PASSWORD, "enc:" + RAW_PASSWORD)).thenReturn(false);

            assertThatThrownBy(() -> service.changePassword(EMAIL, dto))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        void throwsWhenUserNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.changePassword(EMAIL, dto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}