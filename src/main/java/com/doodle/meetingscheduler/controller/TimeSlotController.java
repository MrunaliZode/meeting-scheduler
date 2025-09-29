package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.dto.TimeSlotDTO;
import com.doodle.meetingscheduler.dto.UpdateSlotRequest;
import com.doodle.meetingscheduler.service.TimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
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
    @Operation(summary = "Create time slot", description = "Creates a new time slot for a given user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time slot created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<TimeSlotDTO> createSlot(@PathVariable Long userId,
                                                  @RequestParam String start,
                                                  @RequestParam String end) {
        TimeSlot slot = slotService.createSlot(userId, LocalDateTime.parse(start), LocalDateTime.parse(end));
        return ResponseEntity.status(201).body(slotService.toDTO(slot));
    }

    @PutMapping("/{slotId}/status")
    @Operation(summary = "Mark time slot", description = "Marks the given time slot with specified status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time slot status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Meeting conflicts, slot is already booked"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    public ResponseEntity<TimeSlotDTO> markTimeSlot(@PathVariable Long slotId,
                             @RequestParam String start,
                             @RequestParam String end,
                             @RequestParam String status) {
        TimeSlot updated = slotService.markSlot(slotId, LocalDateTime.parse(start), LocalDateTime.parse(end), SlotStatus.valueOf(status));
        return ResponseEntity.ok(slotService.toDTO(updated));
    }

//    @GetMapping("/users/{userId}")
//    @Operation(summary = "Get time slots for user", description = "Fetch all time slots (by status) for a given user.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Slots for the user by status retrieved successfully"),
//            @ApiResponse(responseCode = "404", description = "User not found")
//    })
//    public ResponseEntity<List<TimeSlot>> getSlotsByStatus(@PathVariable Long userId,
//                                       @RequestParam String from,
//                                       @RequestParam String to, @RequestParam String status) {
//        return ResponseEntity.ok(slotService.getSlotsByStatus(userId, LocalDateTime.parse(from), LocalDateTime.parse(to),
//                SlotStatus.valueOf(status)));
//    }

    @GetMapping("/aggregate")
    @Operation(summary = "Get time slots for users", description = "Lists all the available time slots for the group of users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aggregated slots for the given users retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "400", description = "Invalid Request Parameters")
    })
    public ResponseEntity<List<TimeSlotDTO>> getAggregatedSlots(
            @RequestParam List<Long> users,
            @RequestParam String from,
            @RequestParam String to) {
        List<TimeSlot> aggregatedSlots = slotService.getAggregatedFreeSlots(users,
                LocalDateTime.parse(from),
                LocalDateTime.parse(to));
        return ResponseEntity.ok(slotService.toDTOList(aggregatedSlots));
    }

    @PutMapping
    @Operation(summary = "Update a time slot", description = "Update the time slot for the given slot id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slot updated successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "400", description = "Invalid Request Parameters")
    })
    public ResponseEntity<TimeSlotDTO> updateSlot(@RequestBody UpdateSlotRequest updateSlot) {
        TimeSlot updated = slotService.updateSlot(updateSlot.getSlotId(),
                LocalDateTime.parse(updateSlot.getStart()),
                LocalDateTime.parse(updateSlot.getEnd()),
                SlotStatus.valueOf(updateSlot.getStatus()));
        return ResponseEntity.ok(slotService.toDTO(updated));
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

    @GetMapping("/{userId}")
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
            @RequestParam(required = false) String status) {

        List<TimeSlot> slots = slotService.getSlotsForUser(userId, from, to, status);
        return ResponseEntity.ok(
                slotService.toDTOList(slots)
        );
    }

}

