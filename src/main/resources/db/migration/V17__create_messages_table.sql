CREATE TABLE messages(
    id SERIAL PRIMARY KEY,
    sender_id BIGINT,
    receiver_id BIGINT,
    content TEXT,
    read BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);