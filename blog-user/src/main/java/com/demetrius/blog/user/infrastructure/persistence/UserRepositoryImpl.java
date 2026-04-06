package com.demetrius.blog.user.infrastructure.persistence;

import com.demetrius.blog.user.domain.user.entity.User;
import com.demetrius.blog.user.domain.user.repository.UserRepository;
import com.demetrius.blog.user.infrastructure.persistence.converter.UserConverter;
import com.demetrius.blog.user.infrastructure.persistence.mapper.UserMapper;
import com.demetrius.blog.user.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    public UserRepositoryImpl(UserMapper userMapper, UserConverter userConverter) {
        this.userMapper = userMapper;
        this.userConverter = userConverter;
    }

    @Override
    public User findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return po != null ? userConverter.toDomain(po) : null;
    }

    @Override
    public void save(User user) {
        UserPO po = userConverter.toPO(user);
        if (po.getId() == null) {
            userMapper.insert(po);
            user.setId(po.getId());
        } else {
            userMapper.updateById(po);
        }
    }
}
