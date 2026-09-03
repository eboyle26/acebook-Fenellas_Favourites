package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friend;
import com.makersacademy.acebook.model.Like;
import com.makersacademy.acebook.model.Notification;
import com.makersacademy.acebook.model.Message;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.*;
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
import java.util.Optional;
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

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    MessageRepository messageRepository;


    // ==========================================
    // GET POSTS
    // ==========================================

    @GetMapping("/posts")
    public String index(Model model) {

        List<Post> posts =
                postRepository.findAllByOrderByDateTimeDesc();

        Map<Long, User> users = new HashMap<>();

        Map<Long, Long> likeCounts = new HashMap<>();

        Map<Long, Boolean> userLikes = new HashMap<>();


        // ==========================================
        // GET CURRENT USER
        // ==========================================

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


        // ==========================================
        // GET POST USERS + LIKES
        // ==========================================

        for (Post post : posts) {

            User user =
                    userRepository
                            .findById(post.getUserId())
                            .orElse(null);

            users.put(
                    post.getUserId(),
                    user
            );

            likeCounts.put(
                    post.getId(),
                    likeRepository.countByPostId(post.getId())
            );

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


        // ==========================================
        // GET ACCEPTED FRIENDS
        // ==========================================

        List<Friend> acceptedFriends =
                friendRepository.findByRequesterOrReceiverAndStatus(
                        currentUser,
                        currentUser,
                        Friend.Status.ACCEPTED
                );


        // ==========================================
        // GET RECENT CONVERSATIONS
        // ==========================================

        Map<Long, Message> recentConversations =
                new HashMap<>();

        Map<Long, User> conversationUsers =
                new HashMap<>();


        for (Friend friendship : acceptedFriends) {

            User friend;

            // If current user sent the request,
            // the friend is the receiver
            if (friendship.getRequester().getId()
                    .equals(currentUser.getId())) {

                friend = friendship.getReceiver();

            } else {

                // Otherwise the friend is the requester
                friend = friendship.getRequester();
            }


            // Find the newest message between
            // the current user and this friend
            Optional<Message> latestMessage =
                    messageRepository
                            .findTopBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtDesc(
                                    currentUser.getId(),
                                    friend.getId(),
                                    friend.getId(),
                                    currentUser.getId()
                            );


            // Only add them if they have
            // actually had a conversation
            if (latestMessage.isPresent()) {

                recentConversations.put(
                        friend.getId(),
                        latestMessage.get()
                );

                conversationUsers.put(
                        friend.getId(),
                        friend
                );
            }
        }


        // ==========================================
        // SEND DATA TO THYMELEAF
        // ==========================================

        model.addAttribute(
                "posts",
                posts
        );

        model.addAttribute(
                "users",
                users
        );

        model.addAttribute(
                "post",
                new Post()
        );

        model.addAttribute(
                "likeCounts",
                likeCounts
        );

        model.addAttribute(
                "userLikes",
                userLikes
        );

        model.addAttribute(
                "recentConversations",
                recentConversations
        );

        model.addAttribute(
                "conversationUsers",
                conversationUsers
        );

        // This was missing and caused the
        // Thymeleaf currentUser.id error
        model.addAttribute(
                "currentUser",
                currentUser
        );


        return "posts/index";
    }


    // ==========================================
    // CREATE POST
    // ==========================================

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

        User currentUser =
                userRepository
                        .findByOktaUserId(principal.getSubject())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "User not found in local database"
                                )
                        );

        Long databaseUserId =
                currentUser.getId();

        post.setUserId(
                databaseUserId
        );

        post.setDateTime(
                LocalDateTime.now()
        );


        // ==========================================
        // IMAGE UPLOAD
        // ==========================================

        if (!image.isEmpty()) {

            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {

                Files.createDirectories(
                        uploadPath
                );
            }

            String originalFilename = image.getOriginalFilename();
            String extension = "";

            if (originalFilename != null
                    && originalFilename.contains(".")) {

                extension =
                        originalFilename.substring(
                                originalFilename.lastIndexOf(".")
                        );
            }


            String fileName =
                    UUID.randomUUID() + extension;

            Path filePath =
                    uploadPath.resolve(
                            fileName
                    );


            Files.copy(
                    image.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            post.setImageUrl("/uploads/" + fileName);
        }

        postRepository.save(post);

        // ==========================================
        // SAVE POST
        // ==========================================

        // This also saves any selected song information
        postRepository.save(
                post
        );

        return new RedirectView(
                "/posts"
        );
    }


    // ==========================================
    // DELETE POST
    // ==========================================

    @PostMapping("/posts/{postId}/delete")
    @Transactional
    public RedirectView deletePost(
            @PathVariable Long postId
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


        // Find the post
        Optional<Post> postOptional =
                postRepository.findById(
                        postId
                );


        // If the post doesn't exist,
        // just return to the posts page
        if (postOptional.isEmpty()) {

            return new RedirectView(
                    "/posts"
            );
        }


        Post post =
                postOptional.get();


        // ==========================================
        // OWNERSHIP CHECK
        // ==========================================

        // Make sure the current user owns the post
        if (!post.getUserId().equals(
                currentUser.getId()
        )) {

            return new RedirectView(
                    "/posts"
            );
        }


        // ==========================================
        // DELETE RELATED DATA
        // ==========================================

        // Delete comments belonging to the post
        commentRepository.deleteByPostId(
                postId
        );

        // Delete likes belonging to the post
        likeRepository.deleteByPostId(
                postId
        );

        // Finally delete the post
        postRepository.deleteById(
                postId
        );


        return new RedirectView(
                "/posts"
        );
    }


    // ==========================================
    // LIKE / UNLIKE POST
    // ==========================================

    @PostMapping("/posts/{postId}/like")
    public RedirectView likes(
            @PathVariable Long postId
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


        // Check whether the current user
        // has already liked this post
        likeRepository
                .findByPostIdAndUserId(
                        postId,
                        currentUser.getId()
                )
                .ifPresentOrElse(

                        // Already liked -> remove like
                        existingLike ->
                                likeRepository.delete(existingLike),
                        () -> {
                            Like like = new Like(postId, currentUser.getId());
                            Like savedLike = likeRepository.save(like);

                            Post post = postRepository
                                    .findById(savedLike.getPostId())
                                    .orElseThrow();

                            if (!post.getUserId().equals(currentUser.getId())) {
                                Notification notification = new Notification(
                                        post.getUserId(),
                                        currentUser.getId(),
                                        "POST_LIKE",
                                        savedLike.getId(),
                                        post.getId(),
                                        currentUser.getUsername() + " liked your post"
                                );

                                notificationRepository.save(notification);
                            }
                        }
                );


        return new RedirectView(
                "/posts"
        );
    }
}