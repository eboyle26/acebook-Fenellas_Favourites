package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Message;
import com.makersacademy.acebook.model.Notification;
import com.makersacademy.acebook.model.User;
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
import java.util.List;

@Controller
public class MessageController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

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

        User currentUser = userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User not found in local database"
                        )
                );

        User receiver = userRepository
                .findById(receiverId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Receiver not found"
                        )
                );

        List<Message> messages =
                messageRepository
                        .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
                                currentUser.getId(),
                                receiverId,
                                receiverId,
                                currentUser.getId()
                        );

        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("receiver", receiver);
        model.addAttribute("submittedMessage", new Message());

        return "messages/index";
    }

    @PostMapping("/messages/{receiverId}")
    public RedirectView create(
            @PathVariable Long receiverId,
            @ModelAttribute("submittedMessage") Message submittedMessage
    ) {

        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User currentUser = userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User not found in database"
                        )
                );

        userRepository
                .findById(receiverId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Receiver not found"
                        )
                );

        if (submittedMessage.getContent() == null
                || submittedMessage.getContent().isBlank()) {

            return new RedirectView("/messages/" + receiverId);
        }

        Message newMessage = new Message();

        newMessage.setSenderId(currentUser.getId());
        newMessage.setReceiverId(receiverId);
        newMessage.setContent(submittedMessage.getContent().trim());
        newMessage.setRead(false);
        newMessage.setCreatedAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(newMessage);

        Notification notification = new Notification(
                savedMessage.getReceiverId(),
                currentUser.getId(),
                "NEW_MESSAGE",
                savedMessage.getId(),
                null,
                currentUser.getUsername() + " sent you a message"
        );

        notificationRepository.save(notification);

        return new RedirectView("/messages/" + receiverId);
    }
}