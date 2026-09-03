package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Message;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
            Long senderIdOne,
            Long receiverIdOne,
            Long senderIdTwo,
            Long receiverIdTwo
    );
}
