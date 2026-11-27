package com.smartcity.issuereporting.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** One row per status change / assignment, giving each complaint a full history. */
@Entity
@Table(name = "complaint_updates")
public class ComplaintUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(length = 500)
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ComplaintUpdate() {}
    public ComplaintUpdate(Complaint complaint, Status status, String remark, User updatedBy) {
        this.complaint = complaint; this.status = status; this.remark = remark; this.updatedBy = updatedBy;
    }

    public Long getId() { return id; }
    public Complaint getComplaint() { return complaint; }
    public Status getStatus() { return status; }
    public String getRemark() { return remark; }
    public User getUpdatedBy() { return updatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
