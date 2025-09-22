package com.fitspine.repository;

import com.fitspine.model.UserWearableToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWearableTokenRepository extends JpaRepository<UserWearableToken, Long> {
    Optional<UserWearableToken> findByUserIdAndProvider(Long userId, String provider);
}
