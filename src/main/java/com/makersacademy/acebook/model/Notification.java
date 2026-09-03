package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "NOTIFICATIONS")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "notification_text")
    private String notificationText;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Notification() {
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(
            Long recipientId,
            Long actorId,
            String notificationType,
            Long relatedId,
            Long postId,
            String notificationText
    ) {
        this.recipientId = recipientId;
        this.actorId = actorId;
        this.notificationType = notificationType;
        this.relatedId = relatedId;
        this.postId = postId;
        this.notificationText = notificationText;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
}
