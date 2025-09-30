package com.doodle.meetingscheduler.repository;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles CRUD Operation for 'time_slots' table
 */
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    /**
     * finds the time slots for the given user with specified time slot status
     *
     * @param userId the identity of the user
     * @param status the status of the time slots for the user
     * @return
     */
    List<TimeSlot> findByUserIdAndStatus(Long userId, SlotStatus status);

    /**
     * Finds the paginated time slots for the given user
     *
     * @param userId   the identity of the user
     * @param pageable pagination data
     * @return paginated rows of the time slots for the given user
     */
    Page<TimeSlot> findByUserId(Long userId, Pageable pageable);

    /**
     * Finds the time slots for the user between from and to
     *
     * @param userId the identity of the user
     * @param from   lower time limit
     * @param to     upper time limit
     * @return all the time slots for the user between the specified range
     */
    List<TimeSlot> findByUserIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * Finds the time slots for the user between from and to and status
     *
     * @param userId the identity of the user
     * @param from   lower time limit
     * @param to     upper time limit
     * @param status status of the time slot
     * @return all the time slots for the user between the specified range and status
     */
    List<TimeSlot> findByUserIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
            Long userId,
            LocalDateTime from,
            LocalDateTime to,
            SlotStatus status
    );

    /**
     * Finds the time slots for the given users between from and to and status
     *
     * @param userIds   identity of multiple users
     * @param startTime lower time limit
     * @param endTime   upper time limit
     * @param status    status of the time slot
     * @return all the time slots for all the users between the specified range and status
     */
    @Query("SELECT s FROM TimeSlot s " +
            "WHERE s.user.id IN :userIds " +
            "AND s.startTime >= :startTime " +
            "AND s.endTime <= :endTime " +
            "AND s.status = :status")
    List<TimeSlot> findByUserIdsAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(List<Long> userIds, LocalDateTime startTime, LocalDateTime endTime, SlotStatus status);


}
