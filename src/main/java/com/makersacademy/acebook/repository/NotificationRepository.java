package com.makersacademy.acebook.repository;


import com.makersacademy.acebook.model.Notification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository
        extends CrudRepository<Notification, Long> {

    List<Notification>
    findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification>
    findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    @Transactional
    void deleteByRecipientId(Long recipientId);

    @Transactional
    void deleteByActorId(Long actorId);
}
