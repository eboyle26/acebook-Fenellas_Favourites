package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

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

    @Column(name = "song_title")
    private String songTitle;

    @Column(name = "song_artist")
    private String songArtist;

    @Column(name = "song_image_url")
    private String songImageUrl;

    @Column(name = "song_preview_url")
    private String songPreviewUrl;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    public Post() {}

    public Post(String content, Long userId) {
        this.content = content;
        this.userId = userId;
        this.dateTime = LocalDateTime.now();
    }
}

