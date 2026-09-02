package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Comment;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    Iterable<Comment> findByPostId(Long postId);
    void deleteByPostId(Long postId);
    void deleteById(Long Id);
}
