package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.data.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Handles CRUD Operation for 'users' table
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
