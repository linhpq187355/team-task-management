package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    Optional<Workspace> findByIdAndDeletedAtIsNull(Long id);
}