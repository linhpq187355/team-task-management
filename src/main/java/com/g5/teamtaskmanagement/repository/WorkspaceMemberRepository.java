package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
}