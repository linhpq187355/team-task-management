package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
}