package com.makersacademy.acebook.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class FriendTest {

    // Setting two friends
    final User requester = new User("requesterUser", "okta-1", "requester@example.com", null);
    final User receiver = new User("receiverUser", "okta-2", "receiver@example.com", null);

    @Test
    public void allArgsConstructorSetsAllFields() {
        LocalDateTime createdAt = LocalDateTime.now();
        Friend friend = new Friend(1L, receiver, requester, Friend.Status.PENDING, createdAt);

        assertEquals(1L, friend.getId());
        assertEquals(receiver, friend.getReceiver());
        assertEquals(requester, friend.getRequester());
        assertEquals(Friend.Status.PENDING, friend.getStatus());
        assertEquals(createdAt, friend.getCreatedAt());
    }

    @Test
    public void noArgConstructorLeavesFieldsNull() {
        Friend emptyFriend = new Friend();

        assertNull(emptyFriend.getId());
        assertNull(emptyFriend.getReceiver());
        assertNull(emptyFriend.getRequester());
        assertNull(emptyFriend.getStatus());
        assertNull(emptyFriend.getCreatedAt());
    }

    // check getters and setters

    @Test
    public void canSetAndGetId() {
        Friend friend = new Friend();
        friend.setId(111L);
        assertEquals(111L, friend.getId());
    }

    @Test
    public void canSetAndGetReceiver() {
        Friend friend = new Friend();
        friend.setReceiver(receiver);
        assertEquals(receiver, friend.getReceiver());
    }

    @Test
    public void canSetAndGetRequester() {
        Friend friend = new Friend();
        friend.setRequester(requester);
        assertEquals(requester, friend.getRequester());
    }

    @Test
    public void canSetAndGetStatus() {
        Friend friend = new Friend();
        friend.setStatus(Friend.Status.ACCEPTED);
        assertEquals(Friend.Status.ACCEPTED, friend.getStatus());
    }

    @Test
    public void canSetAndGetCreatedAt() {
        Friend friend = new Friend();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        friend.setCreatedAt(createdAt);
        assertEquals(createdAt, friend.getCreatedAt());
    }


    @Test
    public void statusEnumHasExpectedValues() {
        Friend.Status[] values = Friend.Status.values();

        assertEquals(3, values.length);
        assertNotNull(Friend.Status.valueOf("PENDING"));
        assertNotNull(Friend.Status.valueOf("ACCEPTED"));
        assertNotNull(Friend.Status.valueOf("REJECTED"));
    }



    @Test
    public void statusCanBeUpdatedFromPendingToAccepted() {
        Friend friend = new Friend();
        friend.setStatus(Friend.Status.PENDING);
        friend.setStatus(Friend.Status.ACCEPTED);
        assertEquals(Friend.Status.ACCEPTED, friend.getStatus());
    }

}