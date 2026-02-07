package com.fitspine.repository;

import com.fitspine.enums.AuthProvider;
import com.fitspine.model.User;
import com.fitspine.service.UserIdEmailProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @Modifying
    @Query(
            value = """
                        DELETE FROM users
                        WHERE auth_provider = :provider
                          AND age = 0
                          AND gender = :gender
                    """,
            nativeQuery = true
    )
    int deleteAbandonedGoogleUsersNative(
            @Param("provider") String provider,
            @Param("gender") String gender
    );

    @Query("""
                SELECT 
                    u.id AS id,
                    u.email AS email,
                    u.fullName AS fullName,
                    u.publicId AS publicId
                FROM User u
                WHERE u.emailRemindersEnabled = true
                  AND u.email IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1
                      FROM ManualDailyLog l
                      WHERE l.user.id = u.id
                        AND l.logDate = :date
                  )
            """)
    List<UserIdEmailProjection> findUsersWithoutManualLog(
            @Param("date") LocalDate date
    );

    Optional<User> findByPublicId(String publicId);
}
