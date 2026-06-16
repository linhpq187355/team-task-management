package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.WorkspaceMember;
import com.g5.teamtaskmanagement.enums.WorkspaceMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    boolean existsByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);

    boolean existsByWorkspaceIdAndMemberIdAndRole(Long workspaceId, Long memberId, WorkspaceMemberRole role);

    List<WorkspaceMember> findByWorkspaceId(Long workspaceId);

    List<WorkspaceMember> findByMemberId(Long memberId);

    Optional<WorkspaceMember> findByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);

    void deleteByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);
}
