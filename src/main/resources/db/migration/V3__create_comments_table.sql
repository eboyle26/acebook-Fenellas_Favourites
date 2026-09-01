CREATE TABLE comments (
  id SERIAL PRIMARY KEY,
  post_id INTEGER,
  user_id INTEGER,
  content TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);