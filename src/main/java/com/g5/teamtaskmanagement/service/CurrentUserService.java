package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.entity.User;

public interface CurrentUserService {
    User getCurrentUser();

    Long getCurrentUserId();
}
