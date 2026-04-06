package com.demetrius.blog.auth.domain.user.repository;

import com.demetrius.blog.auth.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    User findById(Long id);

    void save(User user);

    void delete(Long id);
}
