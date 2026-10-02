package com.kricontech.interviewace.repository;

import com.kricontech.interviewace.model.Activity;
import com.kricontech.interviewace.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
