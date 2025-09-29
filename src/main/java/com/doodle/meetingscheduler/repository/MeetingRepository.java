package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.data.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    // Fetch meetings where the user is a participant AND slot is within the given range
    @Query("SELECT m FROM Meeting m " +
            "JOIN m.participants p " +
            "WHERE p.id = :userId " +
            "AND m.slot.startTime >= :from " +
            "AND m.slot.endTime <= :to")
    List<Meeting> findByParticipantAndTimeRange(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    List<Meeting> findBySlotUserId(Long userId);

    @Query("SELECT m FROM Meeting m " +
            "JOIN m.participants p " +
            "WHERE p.id IN :participantIds " +
            "AND m.slot.startTime < :slotEnd " +
            "AND m.slot.endTime > :slotStart")
    List<Meeting> findOverlappingMeetings(
            @Param("participantIds") List<Long> participantIds,
            @Param("slotStart") LocalDateTime slotStart,
            @Param("slotEnd") LocalDateTime slotEnd);
}
