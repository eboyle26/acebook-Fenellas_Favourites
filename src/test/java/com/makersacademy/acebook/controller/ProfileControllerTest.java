package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.model.User;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Optional;

import static org.assertj.core.api.BDDAssertions.and;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getProfileAtSignIn() throws Exception {
        DefaultOidcUser principal = createPrincipal();

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(principal, null)
        );

        User user = new User(
                "testuser",
                "okta-123",
                "test@email.com",
                "https://example.com/photo.jpg"
        );

        when(userRepository.findByOktaUserId("okta-123"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profiles/index"))
                .andExpect(model().attribute("user", user));
    }

    @Test
    void updateUserProfile() throws Exception {
        DefaultOidcUser principal = createPrincipal();

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(principal, null)
        );

        User user = new User(
                "testuser",
                "okta-123",
                "test@email.com",
                "https://example.com/photo.jpg"
        );

        when(userRepository.findByOktaUserId("okta-123"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(post("/profile")
                .param("username", "newUsername")
                .param("profilePictureUrl", "newPicture.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(userRepository).save(user);
    }

    //Helper Method is creating a pretend user
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
