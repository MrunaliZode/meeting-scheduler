package com.doodle.meetingscheduler.service;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.dto.TimeSlotDTO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeSlotService {

    TimeSlot createSlot(Long userId, LocalDateTime start, LocalDateTime end);

    @Transactional
    TimeSlot updateSlot(Long slotId, LocalDateTime newStart, LocalDateTime newEnd, SlotStatus newStatus);

    TimeSlot markSlot(Long slotId, LocalDateTime start, LocalDateTime end, SlotStatus status);

    List<TimeSlot> getSlotsByStatus(Long userId, LocalDateTime from, LocalDateTime to, SlotStatus status);

    List<TimeSlot> getSlotsForUser(Long userId, String from, String to, String status);

    // Helper for range queries without status
    List<TimeSlot> getSlotsInRange(Long userId, LocalDateTime from, LocalDateTime to);

    void deleteSlot(Long slotId);

    List<TimeSlot> getAggregatedFreeSlots(List<Long> userIds, LocalDateTime from, LocalDateTime to);

    TimeSlotDTO toDTO(TimeSlot slot);

    List<TimeSlotDTO> toDTOList(List<TimeSlot> slots);

}

