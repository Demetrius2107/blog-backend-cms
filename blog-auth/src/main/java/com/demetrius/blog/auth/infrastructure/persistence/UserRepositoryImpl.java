package com.demetrius.blog.auth.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demetrius.blog.auth.domain.user.entity.User;
import com.demetrius.blog.auth.domain.user.repository.UserRepository;
import com.demetrius.blog.auth.infrastructure.persistence.converter.UserConverter;
import com.demetrius.blog.auth.infrastructure.persistence.mapper.UserMapper;
import com.demetrius.blog.auth.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    public UserRepositoryImpl(UserMapper userConverter, UserMapper userMapper) {
        this.userMapper = userMapper;
        this.userConverter = new UserConverter();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        UserPO po = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, username)
        );
        return Optional.ofNullable(po).map(userConverter::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.exists(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, username)
        );
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

    @Override
    public void delete(Long id) {
        userMapper.deleteById(id);
    }
}
