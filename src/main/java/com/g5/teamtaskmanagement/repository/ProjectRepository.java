package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);
}
