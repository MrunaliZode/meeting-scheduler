package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.dto.CreateMeetingRequest;
import com.doodle.meetingscheduler.dto.MeetingDTO;
import com.doodle.meetingscheduler.service.MeetingService;
import com.doodle.meetingscheduler.utils.Utility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    @Operation(summary = "Book a meeting", description = "Books a meeting for a specific slot and participants.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting booked successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "409", description = "Slot already booked or conflict detected"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing or invalid participants)")
    })
    public ResponseEntity<MeetingDTO> bookMeeting(@RequestBody CreateMeetingRequest request) {
        Meeting created = meetingService.bookMeeting(
                request.getUserId(),
                request.getTitle(),
                request.getDescription(),
                request.getParticipantIds(),
                request.getFrom(),
                request.getTo()
        );
        return ResponseEntity.status(201).body(Utility.toMeetingDTO(created));
    }

    @GetMapping("/users")
    @Operation(summary = "Get meetings for users", description = "Fetch meetings for one or more users with optional date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meetings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<MeetingDTO>> getMeetingsForUsers(
            @RequestParam List<Long> userIds,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer size) {

        if (userIds == null) {
            userIds = List.of();
        }
        Page<Meeting> meetings = meetingService.getMeetingsForUsers(userIds, from, to, page, size);
        return ResponseEntity.ok(Utility.toMeetingDTOPage(meetings));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meeting by ID", description = "Retrieve details of a meeting by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting with the given id found"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    public ResponseEntity<MeetingDTO> getMeetingById(@PathVariable Long id) {
        Meeting meeting = meetingService.getMeeting(id);
        return ResponseEntity.ok(Utility.toMeetingDTO(meeting));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete meeting", description = "Deletes a meeting and frees its time slot.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }
}

