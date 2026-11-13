package ru.kotov.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kotov.notification.domain.Notification;
import ru.kotov.notification.repository.NotificationRepository;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void sendNotification(Long orderId, String eventType, String recipient, String content) {
        log.info("Sending notification: orderId={}, eventType={}, recipient={}, content={}",
                orderId, eventType, recipient, content);

        Notification notification = new Notification(orderId, eventType, recipient, content, "SENT");
        notificationRepository.save(notification);

        log.info("Notification saved: orderId={}, eventType={}", orderId, eventType);
    }
}
