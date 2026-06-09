package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.WorkItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
}