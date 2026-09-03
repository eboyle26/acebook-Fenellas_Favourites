package com.makersacademy.acebook.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class PostTest {

	final long user_id = 1;
	final Post post = new Post("hello", user_id);

	@Test
	public void postHasContent() {
		assertThat(post.getContent(), containsString("hello"));
	}

	@Test
	public void postHasUserId() {
		assertEquals(user_id, post.getUserId());
	}

	@Test
	public void twoArgConstructorSetsDateTimeAutomatically() {
		assertThat(post.getDateTime(), notNullValue());
	}



	@Test
	public void dateTimeIsSetToApproximatelyNow() {
		LocalDateTime before = LocalDateTime.now().minusSeconds(2);
		Post newPost = new Post("timing test", user_id);
		LocalDateTime after = LocalDateTime.now().plusSeconds(2);

		assertTrue(newPost.getDateTime().isAfter(before));
		assertTrue(newPost.getDateTime().isBefore(after));
	}

	@Test
	public void noArgConstructorLeavesFieldsNull() {
		Post emptyPost = new Post();

		assertNull(emptyPost.getId());
		assertNull(emptyPost.getUserId());
		assertNull(emptyPost.getContent());
		assertNull(emptyPost.getImageUrl());
		assertNull(emptyPost.getDateTime());
	}

	// Testing Lombok functions

	@Test
	public void canSetAndGetPostId() {
		Post newPost = new Post();
		newPost.setId(123L);
		assertEquals(123L, newPost.getId());
	}

	@Test
	public void canSetAndGetUserId() {
		Post newPost = new Post();
		newPost.setUserId(111L);
		assertEquals(111L, newPost.getUserId());
	}

	@Test
	public void canSetAndGetContent() {
		Post newPost = new Post();
		newPost.setContent("some content");
		assertThat(newPost.getContent(), containsString("some content"));
	}

	@Test
	public void canSetAndGetImageUrl() {
		Post newPost = new Post();
		newPost.setImageUrl("http://example.com/image.png");
		assertThat(newPost.getImageUrl(), containsString("example.com"));
	}

	@Test
	public void canSetAndGetDateTime() {
		Post newPost = new Post();
		LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 12, 0);
		newPost.setDateTime(dateTime);
		assertEquals(dateTime, newPost.getDateTime());
	}


	@Test
	public void postsWithDifferentContentAreNotEqualEvenWithSameUser() {
		Post postA = new Post("content A", user_id);
		Post postB = new Post("content B", user_id);

		assertNotEquals(postA, postB);
	}

	@Test
	public void toStringLombokContainsContent() {
		assertThat(post.toString(), containsString("hello"));
	}
}