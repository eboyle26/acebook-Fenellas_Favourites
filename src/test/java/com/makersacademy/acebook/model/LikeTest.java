package com.makersacademy.acebook.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class LikeTest {

    final Like like = new Like(100L, 101L);

    @Test
    public void likeHasPostId() {
        assertEquals(100L, like.getPostId());
    }

    @Test
    public void likeHasUserId() {
        assertEquals(101L, like.getUserId());
    }

    @Test
    public void likeHasCreatedAt() {
        assertThat(like.getCreatedAt(), notNullValue());
    }

    @Test
    public void createdAtIsSetToApproximatelyNow() {
        Like newLike = new Like(105L, 106L);

        LocalDateTime before = LocalDateTime.now().minusSeconds(2);
        LocalDateTime after = LocalDateTime.now().plusSeconds(2);

        assertTrue(newLike.getCreatedAt().isAfter(before));
        assertTrue(newLike.getCreatedAt().isBefore(after));
    }

    @Test
    public void noArgConstructorLeavesFieldsAtDefaults() {
        Like emptyLike = new Like();

        assertNull(emptyLike.getId());
        assertNull(emptyLike.getCreatedAt());
        assertNull(emptyLike.getPostId());
        assertNull(emptyLike.getUserId());
    }


// Tests for Lombok setters and getters

    @Test
    public void canSetAndGetId() {
        Like newLike = new Like();
        newLike.setId(123L);

        assertEquals(123L, newLike.getId());
    }

    @Test
    public void canSetAndGetCreatedAt() {
        Like newLike = new Like();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);

        newLike.setCreatedAt(createdAt);

        assertEquals(createdAt, newLike.getCreatedAt());
    }

    @Test
    public void canSetAndGetPostId() {
        Like newLike = new Like();

        newLike.setPostId(111L);

        assertEquals(111L, newLike.getPostId());
    }

    @Test
    public void canSetAndGetUserId() {
        Like newLike = new Like();

        newLike.setUserId(222L);

        assertEquals(222L, newLike.getUserId());
    }


// Tests that different Like inputs are not equal

    @Test
    public void likesWithDifferentPostIdAreNotEqual() {
        Like likeA = new Like(123L, 456L);
        Like likeB = new Like(789L, 456L);

        assertNotEquals(likeA, likeB);
    }

    @Test
    public void likesWithDifferentUserIdAreNotEqual() {
        Like likeA = new Like(123L, 456L);
        Like likeB = new Like(123L, 789L);

        assertNotEquals(likeA, likeB);
    }


}