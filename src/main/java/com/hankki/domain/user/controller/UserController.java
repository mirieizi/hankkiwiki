package com.hankki.domain.user.controller;

import com.hankki.domain.user.entity.User;
import com.hankki.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

// Spring MVC 용
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService userService;
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Operation(summary = "회원 가입", description = "이메일·비밀번호·이름으로 신규 회원을 등록합니다.")
  @ApiResponses({
	  @ApiResponse(responseCode = "201", description = "회원 생성 성공", content = @Content),
	  @ApiResponse(responseCode = "400", description = "회원 생성 실패")
  })
  
  @PostMapping("/signup")
  public ResponseEntity<Void> signup(
      // Swagger RequestBody 애노테이션은 풀 패키지명으로 쓴다
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "회원 가입 정보", 
        required    = true
      )
      // Spring MVC RequestBody
      @RequestBody User user
  ) {
    User created = userService.signup(user);
    return ResponseEntity.status(201).build();
  }

  @Operation(summary = "회원 조회", description = "ID에 해당하는 회원 정보를 반환합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping("/get/{id}")
  public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.getById(id);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(user);
  }
}
