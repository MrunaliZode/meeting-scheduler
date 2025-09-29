package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/slots")
public class TimeSlotController {

    private final TimeSlotRepository slotRepository;
    public TimeSlotController(TimeSlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @GetMapping
    public ResponseEntity<List<TimeSlot>> getAllSlots() {
        return ResponseEntity.ofNullable(slotRepository.findAll());
    }
}

