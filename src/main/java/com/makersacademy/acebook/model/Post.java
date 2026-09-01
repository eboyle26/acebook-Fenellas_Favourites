 package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "POSTS")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    public Post() {}

    public Post(String content, Long userId) {
        this.content = content;
        this.userId = userId;
    }

}


