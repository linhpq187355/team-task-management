package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.ProjectMember;
import com.g5.teamtaskmanagement.entity.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectIdAndMemberId(Long projectId, Long memberId);

    boolean existsByProjectIdAndMemberIdAndRole(Long projectId, Long memberId, ProjectMemberRole role);

    Optional<ProjectMember> findFirstByProjectIdAndMemberId(Long projectId, Long memberId);

    Optional<ProjectMember> findByProjectIdAndMemberIdAndRole(Long projectId, Long memberId, ProjectMemberRole role);

    List<ProjectMember> findByProjectIdAndMemberId(Long projectId, Long memberId);

    List<ProjectMember> findByProjectId(Long projectId);
}
