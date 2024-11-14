package com.example.notification_service.service;

import com.example.notification_service.model.Notification;
import com.example.notification_service.model.TransactionEvent;
import com.example.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void sendNotification(Notification notification) {
        notificationRepository.save(notification);
        // Здесь можно добавить отправку уведомлений по email или push-уведомлений
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    // Слушатель Kafka для получения уведомлений о транзакциях
    @KafkaListener(topics = "account-events", groupId = "notification-service-group")
    public void handleTransactionEvent(TransactionEvent transactionEvent) {
        String message = String.format("У вас новая транзакция: %s %s",
                transactionEvent.getType(),
                transactionEvent.getAmount());

        Notification notification = new Notification(transactionEvent.getAccountId(), message, new Date());
        sendNotification(notification);
    }
}

