package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.dto.CreateMeetingRequest;
import com.doodle.meetingscheduler.dto.UpdateMeetingRequest;
import com.doodle.meetingscheduler.service.MeetingService;
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
    public Meeting bookMeeting(@RequestBody CreateMeetingRequest request) {
        return meetingService.bookMeeting(request.getSlotId(),
                request.getTitle(),
                request.getDescription(),
                request.getParticipantIds());
    }

    @GetMapping("/users/{userId}")
    public List<Meeting> getUserMeetings(@PathVariable Long userId,
                                         @RequestParam String from,
                                         @RequestParam String to) {
        return meetingService.getMeetingsForUser(userId, LocalDateTime.parse(from), LocalDateTime.parse(to));
    }

    @GetMapping("/{id}")
    public Meeting get(@PathVariable Long id) {
        return meetingService.getMeeting(id);
    }

    @PutMapping("/{id}")
    public Meeting update(@PathVariable Long id, @RequestBody UpdateMeetingRequest request) {
        return meetingService.updateMeeting(id, request.getTitle(), request.getDescription());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
    }
}

