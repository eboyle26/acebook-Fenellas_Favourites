package com.makersacademy.acebook.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class UserTest {

    final User user = new User("janedoe", "okta-123", "jane@example.com", "http://example.com/pic.png");

    @Test
    public void userHasUsername() {
        assertThat(user.getUsername(), containsString("janedoe"));
    }

    @Test
    public void userHasOktaUserId() {
        assertThat(user.getOktaUserId(), containsString("okta-123"));
    }

    @Test
    public void userHasEmail() {
        assertThat(user.getEmail(), containsString("jane@example.com"));
    }

    @Test
    public void userHasProfilePictureUrl() {
        assertThat(user.getProfilePictureUrl(), containsString("example.com/pic.png"));
    }

    @Test
    public void fourArgConstructorDefaultsEnabledToTrue() {
        assertTrue(user.isEnabled());
    }

    @Test
    public void fourArgConstructorSetsCreatedAtAutomatically() {
        assertThat(user.getCreatedAt(), notNullValue());
    }

    // Check if time created is approx. local TimeDate.now, plus minus 2 seconds to account for the
    // microseconds between getting localdate time and making user, so it haas the bracket of current time +- 2 seconds
    // to fall into
    @Test
    public void createdAtIsSetToApproximatelyNow() {
        User newUser = new User("correctTimeUser", "okta-001", "correctTimeUser@example.com", null);
        LocalDateTime before = LocalDateTime.now().minusSeconds(2);
        LocalDateTime after = LocalDateTime.now().plusSeconds(2);

        assertTrue(newUser.getCreatedAt().isAfter(before));
        assertTrue(newUser.getCreatedAt().isBefore(after));
    }


    @Test
    public void noArgConstructorLeavesFieldsAtDefaults() {
        User emptyUser = new User();

        assertNull(emptyUser.getId());
        assertNull(emptyUser.getUsername());
        assertFalse(emptyUser.isEnabled());
        assertNull(emptyUser.getOktaUserId());
        assertNull(emptyUser.getEmail());
        assertNull(emptyUser.getProfilePictureUrl());
        assertNull(emptyUser.getCreatedAt());
    }


    // tests for lombok setter and getter
    @Test
    public void canSetAndGetId() {
        User newUser = new User();
        newUser.setId(123L);
        assertEquals(123L, newUser.getId());
    }

    @Test
    public void canSetAndGetUsername() {
        User newUser = new User();
        newUser.setUsername("newname");
        assertThat(newUser.getUsername(), containsString("newname"));
    }

    @Test
    public void canSetAndGetEnabled() {
        User newUser = new User();
        newUser.setEnabled(true);
        assertTrue(newUser.isEnabled());

        newUser.setEnabled(false);
        assertFalse(newUser.isEnabled());
    }

    @Test
    public void canSetAndGetOktaUserId() {
        User newUser = new User();
        newUser.setOktaUserId("okta-456");
        assertThat(newUser.getOktaUserId(), containsString("okta-456"));
    }

    @Test
    public void canSetAndGetEmail() {
        User newUser = new User();
        newUser.setEmail("test@example.com");
        assertThat(newUser.getEmail(), containsString("test@example.com"));
    }

    @Test
    public void canSetAndGetProfilePictureUrl() {
        User newUser = new User();
        newUser.setProfilePictureUrl("http://example.com/pic.png");
        assertThat(newUser.getProfilePictureUrl(), containsString("example.com/pic.png"));
    }

    @Test
    public void canSetAndGetCreatedAt() {
        User newUser = new User();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        newUser.setCreatedAt(createdAt);
        assertEquals(createdAt, newUser.getCreatedAt());
    }


    // check two different user inputs dont return as the same

    @Test
    public void usersWithDifferentUsernameAreNotEqual() {
        User userA = new User("userA", "okta-1", "a@example.com", null);
        User userB = new User("userB", "okta-1", "a@example.com", null);

        assertNotEquals(userA, userB);
    }

    @Test
    public void usersWithDifferentEnabledStatusAreNotEqual() {
        User userA = new User("sameuser", "okta-1", "same@example.com", null);
        userA.setEnabled(true);

        User userB = new User("sameuser", "okta-1", "same@example.com", null);
        userB.setEnabled(false);

        assertNotEquals(userA, userB);
    }
}