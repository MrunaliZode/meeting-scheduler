package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.dto.UpdateSlotRequest;
import com.doodle.meetingscheduler.service.TimeSlotService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/slots")
public class TimeSlotController {

    private final TimeSlotService slotService;

    public TimeSlotController(TimeSlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping("/users/{userId}")
    public TimeSlot createSlot(@PathVariable Long userId,
                               @RequestParam String start,
                               @RequestParam String end) {
        return slotService.createSlot(userId, LocalDateTime.parse(start), LocalDateTime.parse(end));
    }

    @PutMapping("/{slotId}/status")
    public TimeSlot markBusy(@PathVariable Long slotId,
                             @RequestParam String start,
                             @RequestParam String end,
                             @RequestParam String status) {
        return slotService.markSlot(slotId, LocalDateTime.parse(start), LocalDateTime.parse(end), SlotStatus.valueOf(status));
    }

    @GetMapping("/users/{userId}")
    public List<TimeSlot> getSlotsByStatus(@PathVariable Long userId,
                                       @RequestParam String from,
                                       @RequestParam String to, @RequestParam String status) {
        return slotService.getSlotsByStatus(userId, LocalDateTime.parse(from), LocalDateTime.parse(to),
                SlotStatus.valueOf(status));
    }

    @GetMapping("/aggregate")
    public List<TimeSlot> getAggregatedSlots(
            @RequestParam List<Long> users,
            @RequestParam String from,
            @RequestParam String to) {
        return slotService.getAggregatedFreeSlots(users,
                LocalDateTime.parse(from),
                LocalDateTime.parse(to));
    }

    @PutMapping
    public TimeSlot updateSlot(@RequestBody UpdateSlotRequest updateSlot) {
        return slotService.updateSlot(updateSlot.getSlotId(),
                LocalDateTime.parse(updateSlot.getStart()),
                LocalDateTime.parse(updateSlot.getEnd()),
                SlotStatus.valueOf(updateSlot.getStatus()));
    }

    @DeleteMapping("/{id}")
    public void deleteSlot(@PathVariable Long slotId) {
        slotService.deleteSlot(slotId);
    }

    @GetMapping("/{id}")
    public List<TimeSlot> getSlotsForUser(@PathVariable Long userId) {
        return slotService.getSlotsForUser(userId);
    }
}

