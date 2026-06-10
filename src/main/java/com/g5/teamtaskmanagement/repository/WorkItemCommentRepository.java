package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.WorkItemComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkItemCommentRepository extends JpaRepository<WorkItemComment, Long> {
}