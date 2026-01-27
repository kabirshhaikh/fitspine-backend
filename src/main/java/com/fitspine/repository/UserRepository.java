package com.fitspine.repository;

import com.fitspine.enums.AuthProvider;
import com.fitspine.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u.email FROM User u WHERE u.id = :id")
    Optional<String> findEmailById(@Param("id") Long id);

    @Query("SELECT u.publicId FROM User u WHERE u.id = :id")
    Optional<String> findPublicIdById(@Param("id") Long id);

    Optional<User> findByAuthProviderAndProviderId(
            AuthProvider authProvider,
            String providerId
    );
}
