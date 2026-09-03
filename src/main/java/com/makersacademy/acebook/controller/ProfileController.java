package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friend;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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


    // ==========================================
    // YOUR OWN PROFILE
    // ==========================================

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

        // Get your posts
        List<Post> posts = postRepository
                .findByUserIdOrderByDateTimeDesc(user.getId());

        // Get your accepted friends
        List<Friend> friends = friendRepository
                .findByRequesterOrReceiverAndStatus(
                        user,
                        user,
                        Friend.Status.ACCEPTED
                );

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        model.addAttribute("friends", friends);

        return "profiles/index";
    }


    // ==========================================
    // OTHER USER'S PROFILE
    // ==========================================

    @GetMapping("/profile/{userId}")
    public String viewUserProfile(
            @PathVariable Long userId,
            Model model,
            Authentication authentication
    ) {

        // The user whose profile we are viewing
        User profileUser = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new IllegalStateException("User not found")
                );

        // The user who is currently logged in
        User currentUser = userRepository
                .findByOktaUserId(authentication.getName())
                .orElseThrow();

        // Posts belonging only to this user
        List<Post> posts = postRepository
                .findByUserIdOrderByDateTimeDesc(userId);

        // This user's accepted friends
        List<Friend> friends = friendRepository
                .findByRequesterOrReceiverAndStatus(
                        profileUser,
                        profileUser,
                        Friend.Status.ACCEPTED
                );

        boolean isOwnProfile =
                currentUser.getId().equals(profileUser.getId());

        boolean areFriends = false;
        boolean requestSent = false;
        boolean requestReceived = false;

        Long incomingRequestId = null;

        // Check whether you are already friends
        List<Friend> acceptedFriendships =
                friendRepository.findByRequesterOrReceiverAndStatus(
                        currentUser,
                        currentUser,
                        Friend.Status.ACCEPTED
                );

        for (Friend friend : acceptedFriendships) {

            boolean friendshipExists =
                    (friend.getRequester().getId().equals(currentUser.getId())
                            && friend.getReceiver().getId().equals(profileUser.getId()))
                            ||
                            (friend.getReceiver().getId().equals(currentUser.getId())
                                    && friend.getRequester().getId().equals(profileUser.getId()));

            if (friendshipExists) {
                areFriends = true;
                break;
            }
        }

        // Check whether you have already sent them a request
        List<Friend> outgoingRequests =
                friendRepository.findByRequesterAndStatus(
                        currentUser,
                        Friend.Status.PENDING
                );

        for (Friend friend : outgoingRequests) {

            if (friend.getReceiver().getId().equals(profileUser.getId())) {
                requestSent = true;
                break;
            }
        }

        // Check whether they have sent you a request
        List<Friend> incomingRequests =
                friendRepository.findByReceiverAndStatus(
                        currentUser,
                        Friend.Status.PENDING
                );

        for (Friend friend : incomingRequests) {

            if (friend.getRequester().getId().equals(profileUser.getId())) {
                requestReceived = true;
                incomingRequestId = friend.getId();
                break;
            }
        }

        model.addAttribute("user", profileUser);
        model.addAttribute("posts", posts);
        model.addAttribute("friends", friends);

        model.addAttribute("currentUser", currentUser);

        model.addAttribute("isOwnProfile", isOwnProfile);
        model.addAttribute("areFriends", areFriends);
        model.addAttribute("requestSent", requestSent);
        model.addAttribute("requestReceived", requestReceived);
        model.addAttribute("incomingRequestId", incomingRequestId);

        return "profiles/user";
    }

    // ==========================================
// SEND FRIEND REQUEST FROM PROFILE
// ==========================================

    @PostMapping("/profile/{userId}/friend-request")
    public RedirectView sendFriendRequestFromProfile(
            @PathVariable Long userId,
            Authentication authentication
    ) {

        // The person sending the request
        User requester = userRepository
                .findByOktaUserId(authentication.getName())
                .orElseThrow();

        // The person whose profile we are viewing
        User receiver = userRepository
                .findById(userId)
                .orElseThrow();

        // Don't allow someone to add themselves
        if (requester.getId().equals(receiver.getId())) {
            return new RedirectView("/profile/" + userId);
        }


        // ==========================================
        // CHECK IF ALREADY FRIENDS
        // ==========================================

        List<Friend> acceptedFriendships =
                friendRepository.findByRequesterOrReceiverAndStatus(
                        requester,
                        requester,
                        Friend.Status.ACCEPTED
                );

        for (Friend friend : acceptedFriendships) {

            boolean alreadyFriends =
                    (friend.getRequester().getId().equals(requester.getId())
                            && friend.getReceiver().getId().equals(receiver.getId()))
                            ||
                            (friend.getReceiver().getId().equals(requester.getId())
                                    && friend.getRequester().getId().equals(receiver.getId()));

            if (alreadyFriends) {
                return new RedirectView("/profile/" + userId);
            }
        }


        // ==========================================
        // CHECK IF REQUEST ALREADY SENT
        // ==========================================

        List<Friend> outgoingRequests =
                friendRepository.findByRequesterAndStatus(
                        requester,
                        Friend.Status.PENDING
                );

        for (Friend friend : outgoingRequests) {

            if (friend.getReceiver().getId().equals(receiver.getId())) {
                return new RedirectView("/profile/" + userId);
            }
        }


        // ==========================================
        // CHECK IF THEY ALREADY SENT US A REQUEST
        // ==========================================

        List<Friend> incomingRequests =
                friendRepository.findByReceiverAndStatus(
                        requester,
                        Friend.Status.PENDING
                );

        for (Friend friend : incomingRequests) {

            if (friend.getRequester().getId().equals(receiver.getId())) {
                return new RedirectView("/profile/" + userId);
            }
        }


        // ==========================================
        // CREATE FRIEND REQUEST
        // ==========================================

        Friend friend = new Friend();

        friend.setRequester(requester);
        friend.setReceiver(receiver);
        friend.setStatus(Friend.Status.PENDING);
        friend.setCreatedAt(LocalDateTime.now());

        friendRepository.save(friend);


        // Stay on the person's profile
        return new RedirectView("/profile/" + userId);
    }

    // ==========================================
    // UPDATE PROFILE
    // ==========================================

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam String username,
            @RequestParam(required = false) String profilePictureUrl,
            @RequestParam(required = false) MultipartFile profilePicture
    ) throws IOException {

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


        // If a file was uploaded, use the uploaded file
        if (profilePicture != null && !profilePicture.isEmpty()) {

            Path uploadPath =
                    Paths.get("uploads/profile-pictures");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename =
                    profilePicture.getOriginalFilename();

            String extension = "";

            if (originalFilename != null
                    && originalFilename.contains(".")) {

                extension = originalFilename.substring(
                        originalFilename.lastIndexOf(".")
                );
            }

            String fileName =
                    UUID.randomUUID() + extension;

            Path filePath =
                    uploadPath.resolve(fileName);

            Files.copy(
                    profilePicture.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            user.setProfilePictureUrl(
                    "/uploads/profile-pictures/" + fileName
            );

        } else if (
                profilePictureUrl != null
                        && !profilePictureUrl.isBlank()
        ) {

            // Otherwise use the URL
            user.setProfilePictureUrl(profilePictureUrl);
        }

        userRepository.save(user);

        return "redirect:/profile";
    }


    // ==========================================
    // SAVE FAVOURITE SONG
    // ==========================================

    @PostMapping("/profile/favourite-song")
    public RedirectView saveFavouriteSong(
            @RequestParam String title,
            @RequestParam String artist,
            @RequestParam String imageUrl,
            @RequestParam String previewUrl
    ) {

        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User user = userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow();

        user.setFavouriteSongTitle(title);
        user.setFavouriteSongArtist(artist);
        user.setFavouriteSongImageUrl(imageUrl);
        user.setFavouriteSongPreviewUrl(previewUrl);

        userRepository.save(user);

        return new RedirectView("/profile");
    }


    // ==========================================
    // REMOVE FAVOURITE SONG
    // ==========================================

    @PostMapping("/profile/favourite-song/delete")
    public RedirectView deleteFavouriteSong() {

        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User user = userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow();

        user.setFavouriteSongTitle(null);
        user.setFavouriteSongArtist(null);
        user.setFavouriteSongImageUrl(null);
        user.setFavouriteSongPreviewUrl(null);

        userRepository.save(user);

        return new RedirectView("/profile");
    }


    // ==========================================
    // DELETE PROFILE
    // ==========================================

    @PostMapping("/profile/delete")
    @Transactional
    public RedirectView deleteProfile(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        DefaultOidcUser principal =
                (DefaultOidcUser) authentication.getPrincipal();

        User user = userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow();

        Long userId = user.getId();


        // Delete comments belonging to the user
        commentRepository.deleteByUserId(userId);

        // Delete likes belonging to the user
        likeRepository.deleteByUserId(userId);


        // Delete comments and likes belonging
        // to the user's posts
        Iterable<Post> userPosts =
                postRepository.findByUserId(userId);

        for (Post post : userPosts) {

            commentRepository.deleteByPostId(post.getId());

            likeRepository.deleteByPostId(post.getId());
        }


        // Delete friendships
        friendRepository.deleteByReceiver(user);
        friendRepository.deleteByRequester(user);


        // Delete messages
        messageRepository.deleteBySenderId(userId);
        messageRepository.deleteByReceiverId(userId);


        // Delete posts
        postRepository.deleteByUserId(userId);


        // Finally delete the user
        userRepository.delete(user);


        // Log the user out
        new SecurityContextLogoutHandler()
                .logout(
                        request,
                        response,
                        authentication
                );

        return new RedirectView("/");
    }
}