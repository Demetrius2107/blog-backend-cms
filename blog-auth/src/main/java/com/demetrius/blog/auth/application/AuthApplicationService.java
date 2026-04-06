package com.demetrius.blog.auth.application;

import com.demetrius.blog.auth.domain.user.entity.User;
import com.demetrius.blog.auth.domain.user.repository.UserRepository;
import com.demetrius.blog.auth.domain.user.service.UserDomainService;
import com.demetrius.blog.auth.interfaces.dto.LoginRequest;
import com.demetrius.blog.auth.interfaces.dto.RegisterRequest;
import com.demetrius.blog.auth.interfaces.dto.TokenVO;
import com.demetrius.blog.common.exception.BizException;
import com.demetrius.blog.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;

    public AuthApplicationService(UserRepository userRepository, UserDomainService userDomainService) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
    }

    public TokenVO login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(ErrorCode.USER_NOT_FOUND::toException);

        if (!userDomainService.checkPassword(user, request.getPassword())) {
            throw ErrorCode.USER_PASSWORD_ERROR.toException();
        }

        if (!user.isEnabled()) {
            throw ErrorCode.USER_DISABLED.toException();
        }

        return new TokenVO(
                userDomainService.generateToken(user),
                7200L
        );
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw ErrorCode.USER_ALREADY_EXISTS.toException();
        }

        User user = userDomainService.createUser(request.getUsername(), request.getPassword(), request.getEmail());
        userRepository.save(user);
    }

    public void logout(String token) {
    }
}
