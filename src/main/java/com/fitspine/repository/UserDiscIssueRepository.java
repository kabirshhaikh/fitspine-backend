package com.fitspine.repository;

import com.fitspine.model.UserDiscIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDiscIssueRepository extends JpaRepository<UserDiscIssue, Long> {
    void deleteAllByUserId(Long userId);

    List<UserDiscIssue> findAllByUserId(Long id);
}
