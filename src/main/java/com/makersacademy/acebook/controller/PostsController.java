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



    @GetMapping("/posts")
    public String index(Model model) {

        List<Post> posts =
                postRepository.findAllByOrderByDateTimeDesc();

        Map<Long, User> users = new HashMap<>();

        Map<Long, Long> likeCounts = new HashMap<>();

        Map<Long, Boolean> userLikes = new HashMap<>();



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



        List<Friend> acceptedFriends =
                friendRepository.findByRequesterOrReceiverAndStatus(
                        currentUser,
                        currentUser,
                        Friend.Status.ACCEPTED
                );


        Map<Long, Message> recentConversations =
                new HashMap<>();

        Map<Long, User> conversationUsers =
                new HashMap<>();


        for (Friend friendship : acceptedFriends) {

            User friend;


            if (friendship.getRequester().getId()
                    .equals(currentUser.getId())) {

                friend = friendship.getReceiver();

            } else {


                friend = friendship.getRequester();
            }



            Optional<Message> latestMessage =
                    messageRepository
                            .findTopBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtDesc(
                                    currentUser.getId(),
                                    friend.getId(),
                                    friend.getId(),
                                    currentUser.getId()
                            );



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


        model.addAttribute(
                "currentUser",
                currentUser
        );


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


        postRepository.save(
                post
        );

        return new RedirectView(
                "/posts"
        );
    }


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


        Optional<Post> postOptional =
                postRepository.findById(
                        postId
                );


        if (postOptional.isEmpty()) {

            return new RedirectView(
                    "/posts"
            );
        }


        Post post =
                postOptional.get();


        if (!post.getUserId().equals(
                currentUser.getId()
        )) {

            return new RedirectView(
                    "/posts"
            );
        }


        commentRepository.deleteByPostId(
                postId
        );


        likeRepository.deleteByPostId(
                postId
        );

        postRepository.deleteById(
                postId
        );


        return new RedirectView(
                "/posts"
        );
    }


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


        likeRepository
                .findByPostIdAndUserId(
                        postId,
                        currentUser.getId()
                )
                .ifPresentOrElse(

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