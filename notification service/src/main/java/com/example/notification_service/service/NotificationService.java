package com.example.notification_service.service;

import com.example.notification_service.model.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Notification notification);
    List<Notification> getNotificationsForUser(Long userId);
}

