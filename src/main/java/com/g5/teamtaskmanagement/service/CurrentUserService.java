package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.exception.UnauthorizedException;
import com.g5.teamtaskmanagement.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthenticated");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails customUserDetails)) {
            throw new UnauthorizedException("Unauthenticated");
        }
        return customUserDetails.getUser();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
