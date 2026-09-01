package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private boolean enabled;

    @Column(name = "okta_user_id")
    private String oktaUserId;

    private String email;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User() {
    }

    public User(
            String username,
            String oktaUserId,
            String email,
            String profilePictureUrl
    ) {
        this.username = username;
        this.oktaUserId = oktaUserId;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
        this.enabled = true;
        this.createdAt = LocalDateTime.now();
    }
}
