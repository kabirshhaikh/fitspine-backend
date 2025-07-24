package com.fitspine.repository;

import com.fitspine.model.UserSurgery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSurgeryRepository extends JpaRepository<UserSurgery, Long> {
}
