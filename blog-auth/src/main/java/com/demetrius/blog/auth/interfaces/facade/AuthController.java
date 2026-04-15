package com.demetrius.blog.auth.interfaces.facade;

import com.demetrius.blog.auth.application.AuthApplicationService;
import com.demetrius.blog.auth.interfaces.dto.LoginRequest;
import com.demetrius.blog.auth.interfaces.dto.RegisterRequest;
import com.demetrius.blog.auth.interfaces.dto.TokenVO;
import com.demetrius.blog.common.response.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 鉴权控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    /**
     *
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authApplicationService.login(request));
    }

    /**
     *
     * @param request
     * @return
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authApplicationService.register(request);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authApplicationService.logout(token);
        return Result.success();
    }


//    public Result<Void>  refresh(@RequestHeader("Authorization") String token){
//    }
//}
    
}