package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friend;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.FriendRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class FriendsController {

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/friends")
    public void getAllFriends(Model model, Authentication authentication) {
        String name = authentication.getName();
        User user = userRepository.findUserByUsername(name).orElseThrow();
        List<Friend> friends = friendRepository.findByRequesterOrReceiverAndStatus(user, user, Friend.Status.ACCEPTED);
        model.addAttribute("friends", friends);
    }

    @PostMapping("/friend-request/{userId}")
    public void sendFriendRequest(@PathVariable Long userId, Authentication authentication) {

        User receiver = userRepository.findById(userId).orElseThrow();

        String oktaUserId = authentication.getName();
        User requester = userRepository.findByOktaUserId(oktaUserId).orElseThrow();

        Friend friend = new Friend();

        friend.setRequester(requester);
        friend.setReceiver(receiver);
        friend.setStatus(Friend.Status.PENDING);
        friend.setCreatedAt(LocalDateTime.now());

        friendRepository.save(friend);

        //needs a return statement and needs to be changed from void
    }

}
