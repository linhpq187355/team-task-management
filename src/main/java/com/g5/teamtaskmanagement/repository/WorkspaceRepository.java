package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}