package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.dto.TimeIntervalDTO;
import com.doodle.meetingscheduler.dto.TimeSlotDTO;
import com.doodle.meetingscheduler.dto.UpdateSlotRequest;
import com.doodle.meetingscheduler.service.TimeSlotService;
import com.doodle.meetingscheduler.utils.Utility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
public class TimeSlotController {

    private final TimeSlotService slotService;

    public TimeSlotController(TimeSlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping("/users/{userId}")
    @Operation(summary = "Create time slot", description = "Creates a new time slot for a given user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Time slot created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<TimeSlotDTO> createSlot(@PathVariable Long userId,
                                                  @RequestParam String start,
                                                  @RequestParam String end) {
        TimeSlot slot = slotService.createSlot(userId, start, end);
        return ResponseEntity.status(201).body(Utility.toTimeSlotDTO(slot));
    }

    @GetMapping("/users/{userId}")
    @Operation(
            summary = "Get time slots for user",
            description = "Fetch all time slots for a given user. Optionally filter by date range (from + to must be provided together) and/or status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slots retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<List<TimeSlotDTO>> getSlotsForUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer size) {

        return ResponseEntity.ok(slotService.getSlotsForUser(userId, from, to, status, page, size));
    }

    @GetMapping("/users")
    @Operation(summary = "Get time slots for users", description = "Lists all the available time slots for the group of users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aggregated slots for the given users retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "400", description = "Invalid Request Parameters")
    })
    public ResponseEntity<List<TimeSlot>> getAggregatedSlots(
            @RequestParam List<Long> users,
            @RequestParam String from,
            @RequestParam String to) {
        List<TimeSlot> aggregatedSlots = slotService.getAggregatedFreeSlots(users, from, to);
        return ResponseEntity.ok(aggregatedSlots);
    }

    @GetMapping("/users/availability")
    @Operation(summary = "Get users availability", description = "Lists the common availability of users for the given time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Common available slots for the given users retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "common slot not found"),
            @ApiResponse(responseCode = "400", description = "Invalid Request Parameters")
    })
    public ResponseEntity<List<TimeIntervalDTO>> getCommonSlots(
            @RequestParam List<Long> users,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        List<TimeSlot> commonFreeSlots = slotService.getCommonFreeSlots(users, from, to);
        return ResponseEntity.ok(Utility.toTimeIntervalDTO(commonFreeSlots));
    }

    @GetMapping("/{slotId}")
    @Operation(summary = "Get time slot", description = "Lists the time slot by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time slot by id retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    public ResponseEntity<TimeSlotDTO> getTimeSlotById(@PathVariable Long slotId) {
        return ResponseEntity.ok(Utility.toTimeSlotDTO(slotService.getTimeSlotById(slotId)));
    }

    @PutMapping("/{slotId}")
    @Operation(summary = "Update a time slot", description = "Update the time slot for the given slot id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slot updated successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "400", description = "Invalid Request Parameters")
    })
    public ResponseEntity<TimeSlotDTO> updateSlot(
            @PathVariable Long slotId,
            @RequestBody UpdateSlotRequest updateSlot) {
        TimeSlot updated = slotService.updateSlot(slotId,
                updateSlot.getStart(), updateSlot.getEnd(), updateSlot.getStatus());
        return ResponseEntity.ok(Utility.toTimeSlotDTO(updated));
    }

    @DeleteMapping("/{slotId}")
    @Operation(summary = "Delete time slot", description = "Deletes a time slot if it is not booked.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Time slot deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "409", description = "Slot already booked and cannot be deleted")
    })
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId) {
        slotService.deleteSlot(slotId);
        return ResponseEntity.noContent().build();
    }
}

