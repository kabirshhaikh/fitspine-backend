package com.fitspine.repository;

import com.fitspine.model.UserSurgery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSurgeryRepository extends JpaRepository<UserSurgery, Long> {
    void deleteAllByUserId(Long userId);

    List<UserSurgery> findAllByUserId(Long id);
}
