package com.smartcity.issuereporting.service;

import com.smartcity.issuereporting.dto.Dtos.NotificationDto;
import com.smartcity.issuereporting.model.Notification;
import com.smartcity.issuereporting.model.User;
import com.smartcity.issuereporting.repository.NotificationRepository;
import com.smartcity.issuereporting.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notifications;
    private final UserRepository users;

    public NotificationService(NotificationRepository notifications, UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Transactional
    public void notify(User user, String message, String complaintCode) {
        notifications.save(new Notification(user, message, complaintCode));
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> list(String email) {
        User u = users.findByEmail(email).orElseThrow();
        return notifications.findByUserIdOrderByCreatedAtDesc(u.getId()).stream()
                .map(n -> new NotificationDto(n.getId(), n.getMessage(), n.getComplaintCode(), n.isRead(), n.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void markAllRead(String email) {
        User u = users.findByEmail(email).orElseThrow();
        List<Notification> list = notifications.findByUserIdOrderByCreatedAtDesc(u.getId());
        list.forEach(n -> n.setRead(true));
        notifications.saveAll(list);
    }
}
