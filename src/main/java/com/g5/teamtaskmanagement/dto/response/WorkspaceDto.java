package com.g5.teamtaskmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class WorkspaceDto {
    private Long id;
    private String name;
    private String description;
    private WorkspaceMemberRole myRole;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
