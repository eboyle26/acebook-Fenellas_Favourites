package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Like;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.List;

public interface LikeRepository extends CrudRepository<Like, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    Optional<Like> findByPostId(Long postId);
    Optional<Like>findByPostIdAndUserId(Long postId, Long userId);
    List<Like> findAllByPostId(Long postId);
    Long countByPostId(Long postId);
    void deleteByUserId(Long userId);
    void deleteByPostId(Long postId);
}
