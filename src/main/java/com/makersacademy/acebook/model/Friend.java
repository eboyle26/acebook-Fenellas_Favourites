package com.makersacademy.acebook.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendships")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    public Friend() {}

    public Friend(Long id, User receiver, User requester, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.receiver = receiver;
        this.requester = requester;
        this.status = status;
        this.createdAt = createdAt;
    }


}
