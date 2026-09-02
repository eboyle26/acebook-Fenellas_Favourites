package com.makersacademy.acebook.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "LIKES")
@Getter
@Setter
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "post_id")
    private Long postId;


    @Column(name = "user_id")
    private Long userId;

    public Like(){}

    public Like(Long postId, Long userId) {
        this.createdAt = LocalDateTime.now();
        this.postId = postId;
        this.userId = userId;
    }
}

