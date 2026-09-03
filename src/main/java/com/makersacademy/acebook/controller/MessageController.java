package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Message;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.MessageRepository;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class MessageController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/messages/{receiver_id}")
    public String index(Model model, @PathVariable Long receiver_id){

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

        List<Message> messages = messageRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
                        currentUser.getId(),
                        receiver_id,
                        receiver_id,
                        currentUser.getId()
                );



        model.addAttribute("messages", messages);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("receiverId", receiver_id);
        model.addAttribute("newMessage", new Message());
        return "messages/index";
    }

    @PostMapping("/messages/{receiverId}")
    public RedirectView create(
            @PathVariable Long receiverId,
            @ModelAttribute("newMessage") Message submittedMessage
    ) {
        DefaultOidcUser principal = (DefaultOidcUser)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User currentUser = userRepository
                .findByOktaUserId(principal.getSubject())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User not found in database"
                        )
                );

        userRepository.findById(receiverId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Receiver not found"
                        )
                );

        if (submittedMessage.getContent() == null
                || submittedMessage.getContent().isBlank()) {
            return new RedirectView("/messages/" + receiverId);
        }

        Message newMessage = new Message();

        newMessage.setSenderId(currentUser.getId());
        newMessage.setReceiverId(receiverId);
        newMessage.setContent(submittedMessage.getContent().trim());
        newMessage.setCreatedAt(LocalDateTime.now());

        messageRepository.save(newMessage);

        return new RedirectView("/messages/" + receiverId);
    }
}
