package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    int countByUserId(Long id);

    List<TimeSlot> findByUserIdAndStatus(Long userId, SlotStatus status);

    @Query("SELECT s FROM TimeSlot s WHERE s.user.id IN :userIds AND s.status = :status " +
            "AND s.startTime < :to AND s.endTime > :from")
    List<TimeSlot> findByUserIdInAndStatusAndStartTimeBetween(List<Long> userIds, SlotStatus status, LocalDateTime from, LocalDateTime to);

    List<TimeSlot> findByUserId(Long userId);

    // 🔹 Fetch slots for a user within a time range
    List<TimeSlot> findByUserIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
    );

    // 🔹 Fetch slots for a user within a time range and by status
    List<TimeSlot> findByUserIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
            Long userId,
            LocalDateTime from,
            LocalDateTime to,
            SlotStatus status
    );
}
