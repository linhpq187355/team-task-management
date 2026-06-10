package com.g5.teamtaskmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateWorkspaceRequest {
    @NotBlank(message = "Workspace name is required")
    @Size(min = 1, max = 150, message = "Workspace name must be between 1 and 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}

