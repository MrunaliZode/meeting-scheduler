package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.dto.CreateMeetingRequest;
import com.doodle.meetingscheduler.dto.MeetingDTO;
import com.doodle.meetingscheduler.dto.UpdateMeetingRequest;
import com.doodle.meetingscheduler.service.MeetingService;
import com.doodle.meetingscheduler.utils.Utility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
        Meeting created = meetingService.bookMeeting(request.getSlotId(),
                request.getTitle(),
                request.getDescription(),
                request.getParticipantIds());
        return ResponseEntity.status(201).body(Utility.toMeetingDTO(created));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get meetings for user", description = "Fetch all meetings of a user within a given date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meetings for the user retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<MeetingDTO>> getUserMeetings(@PathVariable Long userId,
                                         @RequestParam String from,
                                         @RequestParam String to) {

        List<Meeting> meetings = meetingService.getMeetingsForUser(userId, from, to);
        return ResponseEntity.ok(Utility.toMeetingDTOList(meetings));
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

    @PutMapping("/{id}")
    @Operation(summary = "Update meeting", description = "Update the title and description of a meeting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting updated successfully"),
            @ApiResponse(responseCode = "404", description = "Meeting not found"),
            @ApiResponse(responseCode = "400", description = "Invalid update request")
    })
    public ResponseEntity<MeetingDTO> updateMeeting(@PathVariable Long id, @RequestBody UpdateMeetingRequest request) {
        Meeting updated = meetingService.updateMeeting(id, request.getTitle(), request.getDescription());
        return ResponseEntity.ok(Utility.toMeetingDTO(updated));
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

