package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.WorkItemAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkItemAttachmentRepository extends JpaRepository<WorkItemAttachment, Long> {
}