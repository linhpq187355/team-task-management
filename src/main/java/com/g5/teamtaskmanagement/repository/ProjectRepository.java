package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}