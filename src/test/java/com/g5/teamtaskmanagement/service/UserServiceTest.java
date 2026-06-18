package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.dto.request.ChangePasswordRequest;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final UserService userService = new UserServiceImpl(userRepository, passwordEncoder, currentUserService);

    @Test
    void changePasswordRejectsWrongOldPassword() {
        User user = user();
        ChangePasswordRequest request = changePasswordRequest();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("Old123!", "old-hash")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.changePassword(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordRejectsSameNewPassword() {
        User user = user();
        ChangePasswordRequest request = changePasswordRequest();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("Old123!", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("New123!", "old-hash")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.changePassword(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordEncodesAndSavesNewPassword() {
        User user = user();
        ChangePasswordRequest request = changePasswordRequest();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("Old123!", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("New123!", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("New123!")).thenReturn("new-hash");

        userService.changePassword(request);

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                "new-hash".equals(saved.getPasswordHash())));
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setPasswordHash("old-hash");
        return user;
    }

    private ChangePasswordRequest changePasswordRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("Old123!");
        request.setNewPassword("New123!");
        request.setRePassword("New123!");
        return request;
    }
}
