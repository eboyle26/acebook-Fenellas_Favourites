package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friend;
import com.makersacademy.acebook.model.Message;
import com.makersacademy.acebook.model.Notification;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.FriendRepository;
import com.makersacademy.acebook.repository.MessageRepository;
import com.makersacademy.acebook.repository.NotificationRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    FriendRepository friendRepository;




    @GetMapping("/messages")
    public String messages(Model model) {

        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User currentUser =
                userRepository
                        .findByOktaUserId(principal.getSubject())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "User not found in local database"
                                )
                        );

        List<Message> allMessages =
                messageRepository
                        .findBySenderIdOrReceiverIdOrderByCreatedAtDesc(
                                currentUser.getId(),
                                currentUser.getId()
                        );

        Map<Long, Message> latestMessages =
                new LinkedHashMap<>();

        for (Message message : allMessages) {

            Long otherUserId;

            if (message.getSenderId().equals(currentUser.getId())) {
                otherUserId = message.getReceiverId();
            } else {
                otherUserId = message.getSenderId();
            }

            if (!latestMessages.containsKey(otherUserId)) {
                latestMessages.put(otherUserId, message);
            }
        }

        List<User> conversationUsers =
                new ArrayList<>();

        for (Long userId : latestMessages.keySet()) {

            userRepository
                    .findById(userId)
                    .ifPresent(conversationUsers::add);
        }

        model.addAttribute(
                "currentUser",
                currentUser
        );

        model.addAttribute(
                "latestMessages",
                latestMessages
        );

        model.addAttribute(
                "conversationUsers",
                conversationUsers
        );

        return "messages/list";
    }


    @GetMapping("/messages/{receiverId}")
    public String index(
            Model model,
            @PathVariable Long receiverId
    ) {

        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User currentUser =
                userRepository
                        .findByOktaUserId(principal.getSubject())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "User not found in local database"
                                )
                        );

        User receiver =
                userRepository
                        .findById(receiverId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Receiver not found"
                                )
                        );




        boolean areFriends =
                friendRepository.existsByRequesterAndReceiverAndStatus(
                        currentUser,
                        receiver,
                        Friend.Status.ACCEPTED
                )
                        ||
                        friendRepository.existsByReceiverAndRequesterAndStatus(
                                currentUser,
                                receiver,
                                Friend.Status.ACCEPTED
                        );

        if (!areFriends) {
            return "redirect:/friends";
        }



        List<Message> messages =
                messageRepository
                        .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
                                currentUser.getId(),
                                receiverId,
                                receiverId,
                                currentUser.getId()
                        );




        model.addAttribute(
                "messages",
                messages
        );

        model.addAttribute(
                "currentUser",
                currentUser
        );

        model.addAttribute(
                "receiver",
                receiver
        );

        model.addAttribute(
                "submittedMessage",
                new Message()
        );


        return "messages/index";
    }




    @PostMapping("/messages/{receiverId}")
    public RedirectView create(
            @PathVariable Long receiverId,
            @ModelAttribute("submittedMessage")
            Message submittedMessage
    ) {

        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User currentUser =
                userRepository
                        .findByOktaUserId(principal.getSubject())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "User not found in database"
                                )
                        );


        User receiver =
                userRepository
                        .findById(receiverId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Receiver not found"
                                )
                        );




        boolean areFriends =
                friendRepository.existsByRequesterAndReceiverAndStatus(
                        currentUser,
                        receiver,
                        Friend.Status.ACCEPTED
                )
                        ||
                        friendRepository.existsByReceiverAndRequesterAndStatus(
                                currentUser,
                                receiver,
                                Friend.Status.ACCEPTED
                        );

        if (!areFriends) {
            return new RedirectView("/friends");
        }




        boolean hasMessage =
                submittedMessage.getContent() != null
                        && !submittedMessage.getContent().isBlank();

        boolean hasSong =
                submittedMessage.getSongTitle() != null
                        && !submittedMessage.getSongTitle().isBlank();


        if (!hasMessage && !hasSong) {

            return new RedirectView(
                    "/messages/" + receiverId
            );
        }




        Message newMessage =
                new Message();

        newMessage.setSenderId(
                currentUser.getId()
        );

        newMessage.setReceiverId(
                receiverId
        );


        if (hasMessage) {

            newMessage.setContent(
                    submittedMessage
                            .getContent()
                            .trim()
            );

        } else {

            newMessage.setContent("");
        }




        if (hasSong) {

            newMessage.setSongTitle(
                    submittedMessage.getSongTitle()
            );

            newMessage.setSongArtist(
                    submittedMessage.getSongArtist()
            );

            newMessage.setSongImageUrl(
                    submittedMessage.getSongImageUrl()
            );

            newMessage.setSongPreviewUrl(
                    submittedMessage.getSongPreviewUrl()
            );
        }


        newMessage.setRead(false);

        newMessage.setCreatedAt(
                LocalDateTime.now()
        );




        Message savedMessage =
                messageRepository.save(
                        newMessage
                );




        Notification notification =
                new Notification(
                        receiverId,
                        currentUser.getId(),
                        "NEW_MESSAGE",
                        savedMessage.getId(),
                        null,
                        currentUser.getUsername()
                                + " sent you a message"
                );

        notificationRepository.save(
                notification
        );


        return new RedirectView(
                "/messages/" + receiverId
        );
    }
}