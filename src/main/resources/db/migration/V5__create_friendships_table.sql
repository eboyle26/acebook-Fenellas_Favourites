CREATE TABLE friendships (
    id SERIAL PRIMARY KEY,
    requester_id INTEGER,
    receiver_id INTEGER,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);