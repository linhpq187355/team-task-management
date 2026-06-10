package com.g5.teamtaskmanagement.dto.response;

import com.g5.teamtaskmanagement.entity.WorkspaceMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberDto {
    private Long id;
    private UserDto user;
    private WorkspaceMemberRole role;
    private LocalDateTime joinedAt;
}
