package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfileController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/profile")
    public String profile(Model model) {

        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        String oktaUserId = principal.getSubject();

        User user = userRepository
                .findByOktaUserId(oktaUserId)
                .orElseThrow();

        model.addAttribute("user", user);

        return "profiles/index";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam String username,
            @RequestParam String profilePictureUrl
    ) {

        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        String oktaUserId = principal.getSubject();

        User user = userRepository
                .findByOktaUserId(oktaUserId)
                .orElseThrow();

        user.setUsername(username);
        user.setProfilePictureUrl(profilePictureUrl);

        userRepository.save(user);

        return "redirect:/profile";
    }
}
