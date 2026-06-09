package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}