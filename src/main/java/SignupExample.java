import org.springframework.boot.SpringApplication;
import com.hankki.domain.user.entity.User;

import com.hankki.HankkiwikiApplication;
import com.hankki.domain.user.service.UserService;

public class SignupExample {
	  public static void main(String[] args) {
	    // (1) Boot 자동설정, application.yml 프로퍼티까지 모두 로드
	    var ctx = SpringApplication.run(HankkiwikiApplication.class);

	    UserService userService = ctx.getBean(UserService.class);
	    User newUser = User.builder()
	        .email("newuse22r@example.com")
	        .password("myPlainPassword")
	        .nickname("newbie222")
	        .dailyUsage(1)
	        .build();
	    User saved = userService.signup(newUser);
	    System.out.println("가입된 유저 ID: " + saved.getId());

	    ctx.close();
	  }
	}

