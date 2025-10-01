package com.doodle.meetingscheduler.utils;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.exceptions.MeetingConflictException;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class that contains common implementations between the services
 */
@Component
public class SlotsHelper {

    private final TimeSlotRepository slotRepository;

    public SlotsHelper(TimeSlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    /**
     * Finds all slots for the given users between specified range and status
     *
     * @param userIds identity of the user
     * @param from    lower bound time search
     * @param to      upper bound time search
     * @param status  status search
     * @return the time slots in the range for the given status
     */
    public List<TimeSlot> getAggregatedSlots(List<Long> userIds,
                                             String from,
                                             String to,
                                             SlotStatus status) {

        LocalDateTime start = (from == null) ? LocalDateTime.now() : Utility.toLocalDateTime(from);
        LocalDateTime end = (to == null) ? LocalDateTime.now().plusYears(1) : Utility.toLocalDateTime(to);
        List<TimeSlot> slots = slotRepository.findByUserIdsAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(userIds, start, end, status);

        return slots;
    }

    /**
     * Gets the common availability of all the users specified
     *
     * @param userIds identities of the users
     * @param from    lower bound time search
     * @param to      upper bound time search
     * @return all common availability of the users in the given range and status
     */
    public List<TimeSlot> getCommonSlots(List<Long> userIds,
                                         String from,
                                         String to) {

        // get all available slots for the range and status
        List<TimeSlot> slots = getAggregatedSlots(userIds, from, to, SlotStatus.FREE);
        // Group slots by user
        Map<Long, List<TimeSlot>> slotsByUser = slots.stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getId()));

        // check if some user has no availability
        if (slotsByUser.size() != userIds.size()) {
            throw new MeetingConflictException("Some users have no availability.");
        }

        // compute intersection
        List<TimeSlot> commonSlots = slotsByUser.values().stream()
                .reduce((list1, list2) -> {
                    List<TimeSlot> intersect = new ArrayList<>();
                    for (TimeSlot s1 : list1) {
                        for (TimeSlot s2 : list2) {
                            // Check if times match exactly (start & end)
                            if (s1.getStartTime().equals(s2.getStartTime())
                                    && s1.getEndTime().equals(s2.getEndTime())) {
                                intersect.add(s1);
                                break;
                            }
                        }
                    }
                    return intersect;
                })
                .orElse(Collections.emptyList());
        return commonSlots;
    }

}
