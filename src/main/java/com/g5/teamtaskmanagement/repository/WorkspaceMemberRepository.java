package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.WorkspaceMember;
import com.g5.teamtaskmanagement.entity.WorkspaceMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    boolean existsByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);

    boolean existsByWorkspaceIdAndMemberIdAndRole(Long workspaceId, Long memberId, WorkspaceMemberRole role);
}
