package com.makersacademy.acebook;

import com.makersacademy.acebook.model.Friend;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestDataLoader implements CommandLineRunner {
    private final CommentRepository commentRepository;
    private final FriendRepository friendRepository;
    private final LikeRepository likeRepository;
    private final MessageRepository messageRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public TestDataLoader(
            UserRepository userRepository,
            MessageRepository messageRepository,
            CommentRepository commentRepository,
            PostRepository postRepository,
            LikeRepository likeRepository,
            FriendRepository friendRepository
            ) {

        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.commentRepository = commentRepository;
        this.friendRepository = friendRepository;
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }


    @Override
    public void run(String... args) {
        System.out.println("Loading test data...");
//        userRepository.deleteAll();
//        postRepository.deleteAll();
//        User testUser = userRepository.save(new User("testuser3", "test-okta-id3", "test3@example.com", null));
//        userRepository.save(new User("testuser2", "test-okta-id2", "test2@example.com", null));
//
//        postRepository.save(new Post("hello", testUser.getId()));


    }
}