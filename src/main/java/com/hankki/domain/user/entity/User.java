package com.hankki.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)       
@Builder                                               
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "user_password", length = 255, nullable = false)
    private String password;

    @Column(name = "user_nickname", length = 20, nullable = false, unique = true)
    private String nickname;

    @Column(name = "user_dailyusage", nullable = false)
    private int dailyUsage;

    public void changeEmail(String email) { this.email = email;    }
    public void changePassword(String password) { this.password = password;    }
    public void changeNickname(String nickname) { this.nickname = nickname;    }
    public void changeDailyUsage(int dailyUsage) { this.dailyUsage = dailyUsage;    }
    
    

}
