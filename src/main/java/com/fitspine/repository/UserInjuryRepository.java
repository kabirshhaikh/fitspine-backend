package com.fitspine.repository;

import com.fitspine.model.UserInjury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInjuryRepository extends JpaRepository<UserInjury, Long> {
}
