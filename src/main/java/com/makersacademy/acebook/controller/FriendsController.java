package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friend;
import com.makersacademy.acebook.model.Notification;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.FriendRepository;
import com.makersacademy.acebook.repository.NotificationRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class FriendsController {

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;


    @GetMapping("/friends")
    public String getAllFriends(Model model, Authentication authentication) {

        String oktaUserId = authentication.getName();

        User user = userRepository
                .findByOktaUserId(oktaUserId)
                .orElseThrow();


        // Accepted friends
        List<Friend> acceptedFriends =
                friendRepository.findByRequesterOrReceiverAndStatus(
                        user,
                        user,
                        Friend.Status.ACCEPTED
                );


        // Incoming friend requests
        List<Friend> pendingFriends =
                friendRepository.findByReceiverAndStatus(
                        user,
                        Friend.Status.PENDING
                );


        // Friend requests YOU have sent
        List<Friend> outgoingPendingFriends =
                friendRepository.findByRequesterAndStatus(
                        user,
                        Friend.Status.PENDING
                );

        List<User> users =
                (List<User>) userRepository.findAll();

        Set<Long> unavailableUserIds = new HashSet<>();

        for (Friend friend : acceptedFriends) {

            if (friend.getRequester().getId().equals(user.getId())) {

                unavailableUserIds.add(
                        friend.getReceiver().getId()
                );

            } else {

                unavailableUserIds.add(
                        friend.getRequester().getId()
                );
            }
        }

        for (Friend friend : outgoingPendingFriends) {

            unavailableUserIds.add(
                    friend.getReceiver().getId()
            );
        }

        model.addAttribute("acceptedFriends", acceptedFriends);
        model.addAttribute("pendingFriends", pendingFriends);
        model.addAttribute("users", users);
        model.addAttribute("currentUser", user);
        model.addAttribute("unavailableUserIds", unavailableUserIds);


        return "friends/index";
    }


    @PostMapping("/friend-request/{userId}")
    public RedirectView sendFriendRequest(
            @PathVariable Long userId,
            Authentication authentication) {

        String oktaUserId = authentication.getName();

        User currentUser = getCurrentUser();

        User requester = userRepository
                .findByOktaUserId(oktaUserId)
                .orElseThrow();

        User receiver = userRepository
                .findById(userId)
                .orElseThrow();

        if (requester.getId().equals(receiver.getId())) {
            return new RedirectView("/friends");
        }

        List<Friend> existingFriendships =
                friendRepository.findByRequesterOrReceiverAndStatus(
                        requester,
                        requester,
                        Friend.Status.ACCEPTED
                );

        for (Friend friend : existingFriendships) {

            boolean alreadyFriends =
                    (friend.getRequester().getId().equals(requester.getId())
                            && friend.getReceiver().getId().equals(receiver.getId()))
                            ||
                            (friend.getReceiver().getId().equals(requester.getId())
                                    && friend.getRequester().getId().equals(receiver.getId()));

            if (alreadyFriends) {
                return new RedirectView("/friends");
            }
        }


        List<Friend> outgoingRequests =
                friendRepository.findByRequesterAndStatus(
                        requester,
                        Friend.Status.PENDING
                );


        for (Friend friend : outgoingRequests) {

            if (friend.getReceiver().getId().equals(receiver.getId())) {
                return new RedirectView("/friends");
            }
        }


        List<Friend> incomingRequests =
                friendRepository.findByReceiverAndStatus(
                        requester,
                        Friend.Status.PENDING
                );


        for (Friend friend : incomingRequests) {

            if (friend.getRequester().getId().equals(receiver.getId())) {
                return new RedirectView("/friends");
            }
        }

        Friend friend = new Friend();

        friend.setRequester(requester);
        friend.setReceiver(receiver);
        friend.setStatus(Friend.Status.PENDING);
        friend.setCreatedAt(LocalDateTime.now());

        Friend savedFriendship = friendRepository.save(friend);

        Notification notification = new Notification(
                savedFriendship.getReceiver().getId(),
                currentUser.getId(),
                "FRIEND_REQUEST",
                savedFriendship.getId(),
                null,
                currentUser.getUsername() + " sent you a friend request"
        );

        notificationRepository.save(notification);


        return new RedirectView("/friends");
    }


    @PostMapping("/friend-request/{friendId}/accept")
    public RedirectView acceptFriendRequest(
            @PathVariable Long friendId) {

        Friend friend =
                friendRepository
                        .findById(friendId)
                        .orElseThrow();

        friend.setStatus(Friend.Status.ACCEPTED);

        friendRepository.save(friend);

        return new RedirectView("/friends");
    }


    @PostMapping("/friend-request/{friendId}/reject")
    public RedirectView rejectFriendRequest(
            @PathVariable Long friendId) {

        Friend friend =
                friendRepository
                        .findById(friendId)
                        .orElseThrow();

        friend.setStatus(Friend.Status.REJECTED);

        friendRepository.save(friend);

        return new RedirectView("/friends");
    }

    @PostMapping("/friends/{friendId}/delete")
    public RedirectView deleteFriend(
            @PathVariable Long friendId,
            Authentication authentication) {

        Friend friend =
                friendRepository
                        .findById(friendId)
                        .orElseThrow();


        String oktaUserId = authentication.getName();

        User currentUser =
                userRepository
                        .findByOktaUserId(oktaUserId)
                        .orElseThrow();

        boolean isRequester =
                friend.getRequester()
                        .getId()
                        .equals(currentUser.getId());

        boolean isReceiver =
                friend.getReceiver()
                        .getId()
                        .equals(currentUser.getId());


        if (!isRequester && !isReceiver) {
            return new RedirectView("/friends");
        }

        friendRepository.delete(friend);

        return new RedirectView("/friends");
    }

    private User getCurrentUser() {
        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        return userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User not found in local database"
                        )
                );
    }
}