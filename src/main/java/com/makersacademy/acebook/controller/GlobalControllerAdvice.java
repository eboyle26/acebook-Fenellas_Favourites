package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.NotificationRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @ModelAttribute("unreadCount")
    public long unreadCount() {

        try {
            DefaultOidcUser principal =
                    (DefaultOidcUser) SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getPrincipal();

            User currentUser =
                    userRepository
                            .findByOktaUserId(principal.getSubject())
                            .orElse(null);

            if (currentUser == null) {
                return 0;
            }

            return notificationRepository
                    .countByRecipientIdAndIsReadFalse(
                            currentUser.getId()
                    );

        } catch (Exception e) {
            return 0;
        }
    }
}
