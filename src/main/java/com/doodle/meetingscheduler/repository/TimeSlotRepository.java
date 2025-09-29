package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.data.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    int countByUserId(Long id);
}
