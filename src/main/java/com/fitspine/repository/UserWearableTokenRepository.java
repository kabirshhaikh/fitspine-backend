package com.fitspine.repository;

import com.fitspine.model.UserWearableToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWearableTokenRepository extends JpaRepository<UserWearableToken, Long> {
}
