package com.g5.teamtaskmanagement.repository;

import com.g5.teamtaskmanagement.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}