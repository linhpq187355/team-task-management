package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);

    List<Project> findByWorkspaceIdAndDeletedAtIsNull(Long workspaceId);

    List<Project> findDistinctByWorkspaceIdAndMembersMemberIdAndDeletedAtIsNull(Long workspaceId, Long memberId);
}
