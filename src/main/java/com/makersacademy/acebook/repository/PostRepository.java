package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Post;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PostRepository extends CrudRepository<Post, Long> {

    List<Post> findAllByOrderByDateTimeDesc();

    void deleteByUserId(Long userId);

    Iterable<Post> findByUserId(Long userId);

    List<Post> findByUserIdOrderByDateTimeDesc(Long userId);
}
