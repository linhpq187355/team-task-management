package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.WorkItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    Optional<WorkItem> findByIdAndDeletedAtIsNull(Long id);
}
