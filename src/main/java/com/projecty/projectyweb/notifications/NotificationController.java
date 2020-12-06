package com.projecty.projectyweb.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("notifications")
@CrossOrigin()
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationService notificationService, NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {
        return new ResponseEntity<>(notificationService.getNotifications(), HttpStatus.OK);
    }

    @GetMapping("unseenCount")
    public ResponseEntity<Long> getUnseenNotificationsCount() {
        return new ResponseEntity<>(notificationService.getUnseenNotificationCount(), HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public void deleteNotification(@PathVariable Long id) {
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (optionalNotification.isPresent()) {
            notificationService.deleteNotification(optionalNotification.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
