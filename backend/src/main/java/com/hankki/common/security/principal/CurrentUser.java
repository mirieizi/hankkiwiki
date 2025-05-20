package com.hankki.common.security.principal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 로그인된 사용자의 UserPrincipal을 가져오는 커스텀 애노테이션.
 * Spring Security의 @AuthenticationPrincipal(expression = \"userPrincipal\") 래퍼입니다.
 *
 * ex) public ResponseEntity<?> getInfo(@CurrentUser UserPrincipal user)
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "userPrincipal")
public @interface CurrentUser {
}
