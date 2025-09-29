package com.doodle.meetingscheduler.service;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeSlotService {

    TimeSlot createSlot(Long userId, String start, String end);

    @Transactional
    TimeSlot updateSlot(Long slotId, String newStart, String newEnd, String newStatus);

    TimeSlot markSlot(Long slotId, String start, String end, String status);

    List<TimeSlot> getSlotsByStatus(Long userId, LocalDateTime from, LocalDateTime to, SlotStatus status);

    List<TimeSlot> getSlotsForUser(Long userId, String from, String to, String status);

    // Helper for range queries without status
    List<TimeSlot> getSlotsInRange(Long userId, LocalDateTime from, LocalDateTime to);

    void deleteSlot(Long slotId);

    List<TimeSlot> getAggregatedFreeSlots(List<Long> userIds, String from, String to);
}

