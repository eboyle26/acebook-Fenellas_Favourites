package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friend;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.FriendRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class FriendsController {

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/friends")
    public void getAllFriends(Model model, Authentication authentication) {
        String oktaUserId = authentication.getName();
        User user = userRepository.findByOktaUserId(oktaUserId).orElseThrow();

        List<Friend> acceptedFriends = friendRepository.findByRequesterOrReceiverAndStatus(user, user, Friend.Status.ACCEPTED);
        List<Friend> pendingFriends = friendRepository.findByReceiverAndStatus(user, Friend.Status.PENDING);


        model.addAttribute("acceptedFriends", acceptedFriends);
        model.addAttribute("pendingFriends", pendingFriends);
    }

    @PostMapping("/friend-request/{userId}")
    public RedirectView sendFriendRequest(@PathVariable Long userId, Authentication authentication) {
    public String getAllFriends(Model model) {

        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User currentUser = userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow();

        // Get current user's accepted friendships
        List<Friend> friends =
                friendRepository.findByRequesterOrReceiverAndStatus(
                        currentUser,
                        currentUser,
                        Friend.Status.ACCEPTED
                );

        // Get all users
        Iterable<User> users = userRepository.findAll();

        model.addAttribute("friends", friends);
        model.addAttribute("users", users);
        model.addAttribute("currentUser", currentUser);

        return "friends/index";
    }

    @PostMapping("/friend-request/{userId}")
    public String sendFriendRequest(
            @PathVariable Long userId
    ) {

        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User requester = userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow();

        User receiver = userRepository
                .findById(userId)
                .orElseThrow();

        Friend friend = new Friend();

        friend.setRequester(requester);
        friend.setReceiver(receiver);
        friend.setStatus(Friend.Status.PENDING);
        friend.setCreatedAt(LocalDateTime.now());

        friendRepository.save(friend);

        return new RedirectView("/friends");
    }

    @PostMapping("/friend-request/{friendId}/accept")
    public RedirectView acceptFriendRequest(@PathVariable Long friendId) {

        Friend friend = friendRepository.findById(friendId).orElseThrow();

        friend.setStatus(Friend.Status.ACCEPTED);

        friendRepository.save(friend);

        return new RedirectView("/friends");
    }

    @PostMapping("/friend-request/{friendId}/reject")
    public RedirectView rejectFriendRequest(@PathVariable Long friendId) {

        Friend friend = friendRepository.findById(friendId).orElseThrow();

        friend.setStatus(Friend.Status.REJECTED);

        friendRepository.save(friend);

        return new RedirectView("/friends");
    }


}
