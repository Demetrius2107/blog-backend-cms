package com.demetrius.blog.user.application;

import com.demetrius.blog.user.domain.user.entity.User;
import com.demetrius.blog.user.domain.user.repository.UserRepository;
import com.demetrius.blog.user.interfaces.dto.UserVO;
import com.demetrius.blog.common.exception.BizException;
import com.demetrius.blog.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class UserApplicationService {

    private final UserRepository userRepository;

    public UserApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserVO getUserById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw ErrorCode.USER_NOT_FOUND.toException();
        }
        return toVO(user);
    }

    public void updateUser(Long id, UserVO vo) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw ErrorCode.USER_NOT_FOUND.toException();
        }
        user.setNickname(vo.getNickname());
        user.setEmail(vo.getEmail());
        user.setAvatar(vo.getAvatar());
        userRepository.save(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus().getCode());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
