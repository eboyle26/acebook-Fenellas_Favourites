package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.CommentRepository;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PostsController {

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

    @GetMapping("/posts")
    public String index(Model model) {

        List<Post> posts = postRepository.findAllByOrderByDateTimeDesc();

        Map<Long, User> users = new HashMap<>();

        for (Post post : posts) {
            User user = userRepository
                    .findById(post.getUserId())
                    .orElse(null);

            users.put(post.getUserId(), user);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("users", users);
        model.addAttribute("post", new Post());

        return "posts/index";
    }

    @PostMapping("/posts")
    public RedirectView create(@ModelAttribute Post post) {

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

        Long databaseUserId = currentUser.getId();

        post.setUserId(databaseUserId);
        post.setDateTime(LocalDateTime.now());

        postRepository.save(post);

        return new RedirectView("/posts");
    }

    @PostMapping("/posts/{postId}/delete")
    @Transactional
    public RedirectView deletePost(@PathVariable Long postId) {
        commentRepository.deleteByPostId(postId);
        postRepository.deleteById(postId);
        return new RedirectView("/posts");
    }
}

