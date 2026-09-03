package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Like;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.LikeRepository;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class PostsController {

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    CommentRepository commentRepository;


    @GetMapping("/posts")
    public String index(Model model) {

        List<Post> posts =
                postRepository.findAllByOrderByDateTimeDesc();

        Map<Long, User> users = new HashMap<>();

        Map<Long, Long> likeCounts = new HashMap<>();

        Map<Long, Boolean> userLikes = new HashMap<>();


        // Get the currently logged-in user
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


        for (Post post : posts) {

            // Get the user who made the post
            User user =
                    userRepository
                            .findById(post.getUserId())
                            .orElse(null);

            users.put(
                    post.getUserId(),
                    user
            );


            // Count how many likes the post has
            likeCounts.put(
                    post.getId(),
                    likeRepository.countByPostId(post.getId())
            );


            // Check whether the current user has liked the post
            userLikes.put(
                    post.getId(),
                    likeRepository
                            .findByPostIdAndUserId(
                                    post.getId(),
                                    currentUser.getId()
                            )
                            .isPresent()
            );
        }


        model.addAttribute("posts", posts);
        model.addAttribute("users", users);
        model.addAttribute("post", new Post());

        model.addAttribute("likeCounts", likeCounts);
        model.addAttribute("userLikes", userLikes);


        return "posts/index";
    }

    @PostMapping("/posts")
    public RedirectView create(
            @ModelAttribute Post post,
            @RequestParam("image") MultipartFile image
    ) throws IOException {

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

        // Only save an image if the user selected one
        if (!image.isEmpty()) {

            // Create uploads folder if it doesn't exist
            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Give the image a unique filename
            String originalFilename = image.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(
                        originalFilename.lastIndexOf(".")
                );
            }

            String fileName = UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(
                    image.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // Store the URL/path in the database
            post.setImageUrl("/uploads/" + fileName);
        }

        // Save the post, including any selected song information
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

    @PostMapping("/posts/{postId}/like")
    public RedirectView likes(@PathVariable Long postId) {

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

        likeRepository
                .findByPostIdAndUserId(postId, currentUser.getId())
                .ifPresentOrElse(
                        existingLike ->
                                likeRepository.delete(existingLike),
                        () ->
                                likeRepository.save(
                                        new Like(
                                                postId,
                                                currentUser.getId()
                                        )
                                )
                );

        return new RedirectView("/posts");
    }
}
