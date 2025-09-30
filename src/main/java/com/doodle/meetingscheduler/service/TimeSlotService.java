package com.doodle.meetingscheduler.service;

import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.dto.TimeSlotDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TimeSlotService {

    /**
     * Create a time slot
     *
     * @param userId identity of the user
     * @param start  start time of the slot
     * @param end    end time of the slot
     * @return created time slot
     */
    TimeSlot createSlot(Long userId, String start, String end);

    /**
     * Updates the time slot
     *
     * @param slotId    identity of the slot
     * @param newStart  updated start time of the slot
     * @param newEnd    updated end time of the slot
     * @param newStatus updated status of the slot
     * @return updated slot
     */
    @Transactional
    TimeSlot updateSlot(Long slotId, String newStart, String newEnd, String newStatus);

    /**
     * Get all time slots (paginated) of the user within the specified range and status
     *
     * @param userId identity of the user
     * @param from   lower bound time search
     * @param to     upper bound time search
     * @param status querying for specific status
     * @param page   page number
     * @param size   size of the page
     * @return the paginated filtered time slots for the given user
     */
    List<TimeSlotDTO> getSlotsForUser(Long userId, String from, String to, String status, Integer page, Integer size);

    /**
     * Deletes the time slot
     *
     * @param slotId identity of the time slot
     */
    void deleteSlot(Long slotId);

    /**
     * Gets all the available slots for the users mentioned between the time period
     *
     * @param userIds identities of the users
     * @param from    lower bound time search
     * @param to      upper bound time search
     * @return the available time slots for the users for the specified range
     */
    List<TimeSlot> getAggregatedFreeSlots(List<Long> userIds, String from, String to);

    /**
     * Finds the time slot
     *
     * @param slotId identity of the time slot
     * @return the time slot
     */
    TimeSlot getTimeSlotById(Long slotId);

    /**
     * Returns the intersecting free time slots for the users
     *
     * @param users identities of the users
     * @param from  lower bound time search
     * @param to    upper bound time search
     * @return all free time slots that are common to the users
     */
    List<TimeSlot> getCommonFreeSlots(List<Long> users, String from, String to);
}

