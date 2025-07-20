package com.swyp10.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swyp10.domain.activitylog.entity.UserActivityLog;
import com.swyp10.domain.review.entity.UserReview;
import com.swyp10.common.BaseTimeEntity;
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

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;
    
    @Column(name = "password", length = 255, nullable = false)
    private String password;
    
    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;
    
    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;
    
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

    // 연관 관계 편의 메서드
    public void addOAuthAccount(OAuthAccount account) {
        this.oauthAccounts.add(account);
        account.setUser(this);
    }

    public void addReview(UserReview review) {
        this.reviews.add(review);
        review.setUser(this);
    }

    public void addActivityLog(UserActivityLog log) {
        this.activityLogs.add(log);
        log.setUser(this);
    }

    // 수정/업데이트 메서드
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

}
