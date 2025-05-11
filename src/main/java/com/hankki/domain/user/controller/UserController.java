package com.hankki.domain.user.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hankki.domain.user.dto.SignUpRequest;
import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.transaction.Transactional;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Transactional
    @Operation(summary = "회원 가입", description = "이메일·비밀번호·닉네임으로 신규 회원을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원 생성 성공", content = @Content),
        @ApiResponse(responseCode = "400", description = "회원 생성 실패")
    })
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원 가입 정보",
            required    = true
        )
        @RequestBody SignUpRequest request
    ) {
        // 서비스 메서드 시그니처: signUp(SignUpRequest) → Long id
        Long newUserId = userService.signUp(request);
        // (선택) 생성된 리소스의 URI를 Location 헤더에 담아주기
        URI location = URI.create("/users/" + newUserId);
        return ResponseEntity.created(location).build();
    }

    @Transactional
    @Operation(summary = "회원 조회", description = "ID에 해당하는 회원 정보를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content),
        @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
