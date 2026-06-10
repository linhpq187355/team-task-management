package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.WorkItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long>, JpaSpecificationExecutor<WorkItem> {
    Optional<WorkItem> findByIdAndDeletedAtIsNull(Long id);
}
