package com.smartcity.issuereporting.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 30)
    private String complaintCode;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}
    public Notification(User user, String message, String complaintCode) {
        this.user = user; this.message = message; this.complaintCode = complaintCode;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getMessage() { return message; }
    public String getComplaintCode() { return complaintCode; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
