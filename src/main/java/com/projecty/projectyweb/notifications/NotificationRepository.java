package com.projecty.projectyweb.notifications;

import com.projecty.projectyweb.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);

    Long countByUserAndSeenFalse(User user);
}
