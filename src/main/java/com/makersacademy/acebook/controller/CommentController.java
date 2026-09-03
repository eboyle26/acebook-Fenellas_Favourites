package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Comment;
import com.makersacademy.acebook.model.Notification;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.CommentRepository;
import com.makersacademy.acebook.repository.NotificationRepository;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CommentController {

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @GetMapping("/posts/{postId}/comments")
    public String index(
            @PathVariable Long postId,
            Model model
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        List<Comment> comments =
                commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("comment", new Comment());
        model.addAttribute("userRepository", userRepository);

        return "comments/index";
    }

    @PostMapping("/posts/{postId}/comments")
    public RedirectView create(
            @PathVariable Long postId,
            @ModelAttribute Comment comment
    ) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        DefaultOidcUser principal = (DefaultOidcUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        User currentUser = userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow(() -> new IllegalStateException(
                        "Logged-in user was not found"
                ));

        comment.setPostId(postId);
        comment.setUserId(currentUser.getId());
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        Post post = postRepository
                .findById(savedComment.getPostId())
                .orElseThrow();

        if (!post.getUserId().equals(currentUser.getId())) {
            Notification notification = new Notification(
                    post.getUserId(),
                    currentUser.getId(),
                    "POST_COMMENT",
                    savedComment.getId(),
                    post.getId(),
                    currentUser.getUsername() + " commented on your post"
            );

            notificationRepository.save(notification);
        }

        return new RedirectView("/posts/" + postId + "/comments");
    }

    @PostMapping("/posts/{postId}/comments/{id}/delete")
    public RedirectView deleteComment(
            @PathVariable Long postId,
            @PathVariable Long id
    ) {
        commentRepository.deleteById(id);

        return new RedirectView("/posts/" + postId + "/comments");
    }
}