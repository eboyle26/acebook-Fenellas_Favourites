package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.Friend;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.FriendRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

@Controller
public class FriendsController {

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/friends")
    public void getAllFriends(Model model, Authentication authentication) {
        String name = authentication.getName();
        User user = userRepository.findUserByUsername(name).orElseThrow();
        List<Friend> friends = friendRepository.findByRequesterOrReceiverAndStatus(user, user, Friend.Status.ACCEPTED);
        model.addAttribute("friends", friends);
    }

}
