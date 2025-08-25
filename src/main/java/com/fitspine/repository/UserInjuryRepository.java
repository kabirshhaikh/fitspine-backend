package com.fitspine.repository;

import com.fitspine.model.UserInjury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInjuryRepository extends JpaRepository<UserInjury, Long> {
    void deleteAllByUserId(Long userId);
    List<UserInjury> findAllByUserId(Long id);
}
