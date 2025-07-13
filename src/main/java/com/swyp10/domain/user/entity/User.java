package com.swyp10.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swyp10.domain.activitylog.entity.UserActivityLog;
import com.swyp10.domain.review.entity.UserReview;
import com.swyp10.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password", callSuper = true)
public class User extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "email", length = 255, nullable = false)
    private String email;
    
    @Column(name = "password", length = 255, nullable = false)
    private String password;
    
    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;
    
    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", length = 50, nullable = false)
    private LoginType loginType;
    
    @Column(name = "signup_completed", nullable = false)
    @Builder.Default
    private Boolean signupCompleted = false;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<OAuthAccount> oauthAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<UserReview> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<UserActivityLog> activityLogs = new ArrayList<>();

    // 비즈니스 메서드들
    public boolean isOAuthUser() {
        return loginType != LoginType.EMAIL;
    }
    
    public boolean isEmailUser() {
        return loginType == LoginType.EMAIL;
    }
}
