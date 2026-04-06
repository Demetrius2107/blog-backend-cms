package com.demetrius.blog.user.infrastructure.persistence.converter;

import com.demetrius.blog.user.domain.user.entity.User;
import com.demetrius.blog.user.domain.user.valueobject.UserStatus;
import com.demetrius.blog.user.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public User toDomain(UserPO po) {
        if (po == null) return null;
        return User.builder()
                .id(po.getId())
                .username(po.getUsername())
                .email(po.getEmail())
                .nickname(po.getNickname())
                .avatar(po.getAvatar())
                .status(UserStatus.of(po.getStatus()))
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    public UserPO toPO(User domain) {
        if (domain == null) return null;
        UserPO po = new UserPO();
        po.setId(domain.getId());
        po.setUsername(domain.getUsername());
        po.setEmail(domain.getEmail());
        po.setNickname(domain.getNickname());
        po.setAvatar(domain.getAvatar());
        po.setStatus(domain.getStatus().getCode());
        po.setCreateTime(domain.getCreateTime());
        po.setUpdateTime(domain.getUpdateTime());
        return po;
    }
}
