CREATE TABLE notifications (
   id BIGINT PRIMARY KEY,
   recipient_id BIGINT NOT NULL,
   actor_id BIGINT NOT NULL,
   notification_type VARCHAR(30) NOT NULL,
   related_id BIGINT,
   post_id BIGINT,
   notification_text VARCHAR(255),
   is_read BOOLEAN DEFAULT FALSE,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);