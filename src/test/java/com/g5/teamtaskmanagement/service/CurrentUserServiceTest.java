package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.exception.UnauthorizedException;
import com.g5.teamtaskmanagement.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserReturnsUserFromCustomPrincipal() {
        User user = new User();
        user.setId(7L);
        user.setEmail("user@example.com");
        user.setFullName("Test User");

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals(user, currentUserService.getCurrentUser());
        assertEquals(7L, currentUserService.getCurrentUserId());
    }

    @Test
    void getCurrentUserThrowsWhenUnauthenticated() {
        assertThrows(UnauthorizedException.class, currentUserService::getCurrentUser);
    }
}
