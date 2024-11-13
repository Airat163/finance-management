package com.example.notification_service.service;

import com.example.notification_service.model.Notification;
import com.example.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;


    @Test
    void testSendNotification() {
        Notification notification = new Notification(1L, "Test Message", new Date());
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.sendNotification(notification);

        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void testGetNotificationsForUser() {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification(1L, "Message 1", new Date()));
        when(notificationRepository.findByUserId(anyLong())).thenReturn(notifications);

        List<Notification> result = notificationService.getNotificationsForUser(1L);

        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByUserId(1L);
    }
}
