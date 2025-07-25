package com.swyp10.domain.auth.entity;

import com.swyp10.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oauth_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class OAuthAccount extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth_id")
    private Long oauthId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private LoginType provider;
    
    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId; // 카카오 고유 ID (4338095876)
    
    @Column(name = "provider_email")
    private String providerEmail; // 카카오에서 받은 이메일
    
    @Column(name = "provider_nickname")
    private String providerNickname; // 카카오 닉네임
    
    @Column(name = "provider_profile_image")
    private String providerProfileImage; // 카카오 프로필 이미지
}
