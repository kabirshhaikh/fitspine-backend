package com.fitspine.repository;

import com.fitspine.model.UserDiscIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDiscIssueRepository extends JpaRepository<UserDiscIssue, Long> {
}
