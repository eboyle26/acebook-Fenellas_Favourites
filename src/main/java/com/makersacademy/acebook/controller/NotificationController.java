package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Notification;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.NotificationRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
public class NotificationController {

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @GetMapping("/notifications")
    public String index(Model model) {
        User currentUser = getCurrentUser();

        List<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(
                        currentUser.getId()
                );

        long unreadCount = notificationRepository
                .countByRecipientIdAndIsReadFalse(
                        currentUser.getId()
                );

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "notifications/index";
    }

    @PostMapping("/notifications/{id}/open")
    public RedirectView openNotification(
            @PathVariable Long id
    ) {
        User currentUser = getCurrentUser();

        Notification notification = notificationRepository
                .findByIdAndRecipientId(id, currentUser.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND
                        )
                );

        notification.setRead(true);
        notificationRepository.save(notification);

        return switch (notification.getNotificationType()) {
            case "NEW_MESSAGE" ->
                    new RedirectView(
                            "/messages/" + notification.getActorId()
                    );

            case "FRIEND_REQUEST" ->
                    new RedirectView("/friends");

            case "FRIEND_REQUEST_ACCEPTED" ->
                    new RedirectView("/profile/" + notification.getActorId());

            case "POST_LIKE", "POST_COMMENT" -> {

                boolean postExists =
                        postRepository.existsById(
                                notification.getPostId()
                        );

                if (!postExists) {
                    yield new RedirectView(
                            "/notifications?message=post-deleted"
                    );
                }

                yield new RedirectView(
                        "/posts/" + notification.getPostId() + "/comments"
                );
            }

            default ->
                    new RedirectView("/notifications");
        };
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
