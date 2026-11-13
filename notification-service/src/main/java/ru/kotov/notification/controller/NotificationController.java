package ru.kotov.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kotov.notification.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> send(
            @RequestParam Long orderId,
            @RequestParam String eventType,
            @RequestParam String recipient,
            @RequestParam String content) {
        notificationService.sendNotification(orderId, eventType, recipient, content);
        return ResponseEntity.ok().build();
    }
}
