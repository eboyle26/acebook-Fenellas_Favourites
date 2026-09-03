package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "MESSAGES")
@Getter
@Setter
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    private String content;

    private Boolean read;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    public Message(
            Long senderId,
            Long receiverId,
            String content,
            Boolean read,
            LocalDateTime createdAt
    ) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.read = read;
        this.createdAt = createdAt;
    }
}