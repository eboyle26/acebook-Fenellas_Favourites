package com.makersacademy.acebook.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class CommentTest {

    final long post_id = 1;
    final long user_id = 2;
    final Comment comment = new Comment(post_id, user_id, "hello");

    @Test
    public void commentHasContent() {
        assertThat(comment.getContent(), containsString("hello"));
    }

    @Test
    public void commentHasPostId() {
        assertEquals(1, comment.getPostId());
        assertEquals(post_id, comment.getPostId());
    }

    @Test
    public void commentHasUserId() {
        assertEquals(2, comment.getUserId());
        assertEquals(user_id, comment.getUserId());
        assertNotEquals(1, comment.getUserId());
    }

    @Test
    public void threeArgConstructorSetsCreatedAtAutomatically() {
        assertThat(comment.getCreatedAt(), notNullValue());
    }

    @Test
    public void createdAtIsSetToApproximatelyNow() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(2);
        Comment newComment = new Comment(post_id, user_id, "timing test");
        LocalDateTime after = LocalDateTime.now().plusSeconds(2);

        assertTrue(newComment.getCreatedAt().isAfter(before));
        assertTrue(newComment.getCreatedAt().isBefore(after));
    }



    @Test
    public void noArgConstructorLeavesFieldsNull() {
        Comment emptyComment = new Comment();

        assertNull(emptyComment.getId());
        assertNull(emptyComment.getPostId());
        assertNull(emptyComment.getUserId());
        assertNull(emptyComment.getContent());
        assertNull(emptyComment.getCreatedAt());
    }


    //Testing lombok

    @Test
    public void canSetAndGetId() {
        Comment newComment = new Comment();
        newComment.setId(123L);
        assertEquals(123L, newComment.getId());
    }

    @Test
    public void canSetAndGetPostId() {
        Comment newComment = new Comment();
        newComment.setPostId(111L);
        assertEquals(111L, newComment.getPostId());
    }

    @Test
    public void canSetAndGetUserId() {
        Comment newComment = new Comment();
        newComment.setUserId(100L);
        assertEquals(100L, newComment.getUserId());
    }

    @Test
    public void canSetAndGetContent() {
        Comment newComment = new Comment();
        newComment.setContent("some words");
        assertThat(newComment.getContent(), containsString("some words"));
    }

    @Test
    public void canSetAndGetCreatedAt() {
        Comment newComment = new Comment();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        newComment.setCreatedAt(createdAt);
        assertEquals(createdAt, newComment.getCreatedAt());
    }


    @Test
    public void commentsWithDifferentContentAreNotEqual() {
        Comment commentA = new Comment(post_id, user_id, "content A");
        Comment commentB = new Comment(post_id, user_id, "content B");

        assertNotEquals(commentA, commentB);
    }

    @Test
    public void commentsWithDifferentPostIdAreNotEqual() {
        Comment commentA = new Comment(1L, user_id, "same content");
        Comment commentB = new Comment(2L, user_id, "same content");

        assertNotEquals(commentA, commentB);
    }

    @Test
    public void toStringContainsContent() {
        assertThat(comment.toString(), containsString("hello"));
    }
}