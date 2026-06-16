package com.g5.teamtaskmanagement.dto.request;

import com.g5.teamtaskmanagement.enums.WorkspaceMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateWorkspaceMemberRoleRequest {
    @NotNull(message = "Role is required")
    private WorkspaceMemberRole role;
}
