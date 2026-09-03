package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.repository.NotificationRepository;
import com.makersacademy.acebook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsersController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private NotificationRepository notificationRepository;

    @Test
    void existingUserNotCreatedAgain() throws Exception {
        DefaultOidcUser principal = createPrincipal();

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(principal, null)
        );

        User existingUser = new User(
                "test@email.com",
                "okta-123",
                "test@email.com",
                "https://example.com/photo.jpg"
        );

        when(userRepository.findByOktaUserId("okta-123"))
                .thenReturn(Optional.of(existingUser));

        mockMvc.perform(get("/users/after-login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void newUserCreated() throws Exception {
        DefaultOidcUser principal = createPrincipal();

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(principal, null)
        );

        when(userRepository.findByOktaUserId("okta-123"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/users/after-login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        verify(userRepository).save(any(User.class));
    }

    //Helper Method
    private DefaultOidcUser createPrincipal() {
        OidcIdToken idToken = OidcIdToken.withTokenValue("fake-token")
                .subject("okta-123")
                .claim("email", "test@email.com")
                .claim("picture", "https://example.com/photo.jpg")
                .build();

        OidcUserInfo userInfo = new OidcUserInfo(
                java.util.Map.of(
                 "sub", "okta-123",
                 "email", "test@email.com",
                 "picture", "https://example.com/photo.jpg"
                )
        );

        return new DefaultOidcUser(
                java.util.List.of(),
                idToken,
                userInfo
        );
    }
}
