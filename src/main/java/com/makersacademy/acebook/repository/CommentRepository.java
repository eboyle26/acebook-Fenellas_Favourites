package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Comment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    Iterable<Comment> findByPostId(Long postId);
    List<Comment> findAllByOrderByCreatedAtAsc();
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    void deleteByPostId(Long postId);
    void deleteById(Long Id);
}
