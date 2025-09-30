package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.data.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles CRUD Operation for 'meetings' table
 */
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /**
     * This query returns all the rows that matches the given time range from the
     * time slots for all the users specified in the @param participantIds.
     *
     * @param participantIds lists all the participants of the meeting
     * @param from           meeting start time
     * @param to             meeting end time
     * @return returns the union of all time slots available within the specified period for all participants
     */
    @Query("SELECT DISTINCT m FROM Meeting m " +
            "JOIN m.participants p " +
            "JOIN m.slots s " +
            "WHERE p.id IN :participantIds " +
            "AND s.startTime >= :from " +
            "AND s.endTime <= :to")
    List<Meeting> findMeetingsByParticipantIdsAndStartTimeGreaterThanEqualsAndEndTimeLessThanEquals(List<Long> participantIds, LocalDateTime from, LocalDateTime to);

}
