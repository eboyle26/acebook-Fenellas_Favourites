package com.makersacademy.acebook.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "COMMENTS")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id")
    private Long postId;


    @Column(name = "user_id")
    private Long userId;

    private String content;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Comment() {
    }

    public Comment(
            Long postId,
            Long userId,
            String content
    ) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
