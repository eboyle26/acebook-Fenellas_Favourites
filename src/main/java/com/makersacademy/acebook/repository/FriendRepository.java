package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Friend;
import com.makersacademy.acebook.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FriendRepository extends CrudRepository<Friend, Long> {

    List<Friend> findByRequesterOrReceiverAndStatus(
            User requester,
            User receiver,
            Friend.Status status
    );

    List<Friend> findByRequesterAndStatus(
            User requester,
            Friend.Status status
    );

    List<Friend> findByReceiverAndStatus(
            User receiver,
            Friend.Status status
    );

    boolean existsByRequesterAndReceiverAndStatus(
            User requester,
            User receiver,
            Friend.Status status
    );

    boolean existsByReceiverAndRequesterAndStatus(
            User receiver,
            User requester,
            Friend.Status status
    );

    void deleteByReceiver(User user);

    void deleteByRequester(User user);
}