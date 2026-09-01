package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Friend;
import com.makersacademy.acebook.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FriendRepository extends CrudRepository<Friend, Long> {
    List<Friend> findByReceiverAndStatus (
            User receiver,
            Friend.Status status
    );

}
