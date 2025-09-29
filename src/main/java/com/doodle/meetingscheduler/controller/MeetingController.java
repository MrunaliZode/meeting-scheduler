package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.dto.CreateMeetingRequest;
import com.doodle.meetingscheduler.dto.UpdateMeetingRequest;
import com.doodle.meetingscheduler.service.MeetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    public ResponseEntity<Meeting> bookMeeting(@RequestBody CreateMeetingRequest request) {
        return ResponseEntity.status(201).body(meetingService.bookMeeting(request.getSlotId(),
                request.getTitle(),
                request.getDescription(),
                request.getParticipantIds()));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Meeting>> getUserMeetings(@PathVariable Long userId,
                                         @RequestParam String from,
                                         @RequestParam String to) {
        return ResponseEntity.ok(meetingService.getMeetingsForUser(userId, LocalDateTime.parse(from), LocalDateTime.parse(to)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meeting> getMeetingById(@PathVariable Long id) {
        return ResponseEntity.ok(meetingService.getMeeting(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meeting> update(@PathVariable Long id, @RequestBody UpdateMeetingRequest request) {
        return ResponseEntity.ok(meetingService.updateMeeting(id, request.getTitle(), request.getDescription()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }
}

