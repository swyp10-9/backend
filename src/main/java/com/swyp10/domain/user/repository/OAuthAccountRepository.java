package com.swyp10.domain.user.repository;

import com.swyp10.domain.user.entity.LoginType;
import com.swyp10.domain.user.entity.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderUserId(LoginType provider, String providerUserId);
}
