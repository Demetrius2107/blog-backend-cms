package com.demetrius.blog.user.domain.user.repository;

import com.demetrius.blog.user.domain.user.entity.User;

public interface UserRepository {

    User findById(Long id);

    void save(User user);
}
