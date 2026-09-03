package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ProfileController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MessageRepository messageRepository;

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

    @PostMapping("/profile")
    @Transactional
    public RedirectView deleteProfile(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        DefaultOidcUser principal =
                (DefaultOidcUser) authentication.getPrincipal();

        User user = userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow();

        Long userId = user.getId();

        Iterable<Post> userPosts = postRepository.findByUserId(userId);

        for (Post post : userPosts) {
            commentRepository.deleteByPostId(post.getId());
            likeRepository.deleteByPostId(post.getId());
        }

        commentRepository.deleteByUserId(userId);
        likeRepository.deleteByUserId(userId);

        friendRepository.deleteByReceiver(user);
        friendRepository.deleteByRequester(user);

        messageRepository.deleteBySenderId(userId);
        messageRepository.deleteByReceiverId(userId);

        postRepository.deleteByUserId(userId);
        userRepository.delete(user);

        new SecurityContextLogoutHandler()
                .logout(request, response, authentication);

        return new RedirectView("/");
    }


}
