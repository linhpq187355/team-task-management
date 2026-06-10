package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.ProjectMember;
import com.g5.teamtaskmanagement.entity.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectIdAndMemberId(Long projectId, Long memberId);

    boolean existsByProjectIdAndMemberIdAndRole(Long projectId, Long memberId, ProjectMemberRole role);
}
