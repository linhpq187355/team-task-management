package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.WorkItemActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkItemActivityLogRepository extends JpaRepository<WorkItemActivityLog, Long> {
}