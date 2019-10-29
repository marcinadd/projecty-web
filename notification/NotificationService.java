package com.projecty.projectyweb.notification;


import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
@Transactional
public class NotificationService {
    private  NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public long getNotifications(Long notificationId) {
       
        return 0; //TODO: Implement this method
    }
    
    public void removeNotification(Notification notification) {
    
    }
}

