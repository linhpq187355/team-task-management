package com.g5.teamtaskmanagement.mapper;

import com.g5.teamtaskmanagement.dto.response.UserDto;
import com.g5.teamtaskmanagement.dto.response.WorkspaceDto;
import com.g5.teamtaskmanagement.dto.response.WorkspaceMemberDto;
import com.g5.teamtaskmanagement.entity.Workspace;
import com.g5.teamtaskmanagement.entity.WorkspaceMember;
import com.g5.teamtaskmanagement.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    public WorkspaceMapper(WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public WorkspaceDto toDto(Workspace workspace, Long userId) {
        WorkspaceDto dto = new WorkspaceDto();
        dto.setId(workspace.getId());
        dto.setName(workspace.getName());
        dto.setDescription(workspace.getDescription());
        dto.setCreatedAt(workspace.getCreatedAt());

        workspaceMemberRepository.findByWorkspaceIdAndMemberId(workspace.getId(), userId)
                .ifPresent(member -> dto.setMyRole(member.getRole()));

        return dto;
    }

    public WorkspaceMemberDto toMemberDto(WorkspaceMember member) {
        WorkspaceMemberDto dto = new WorkspaceMemberDto();
        dto.setId(member.getId());
        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());

        UserDto userDto = new UserDto();
        userDto.setId(member.getMember().getId());
        userDto.setEmail(member.getMember().getEmail());
        userDto.setFullName(member.getMember().getFullName());
        dto.setUser(userDto);

        return dto;
    }
}
