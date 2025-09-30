package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.dto.TimeSlotDTO;
import com.doodle.meetingscheduler.dto.UpdateSlotRequest;
import com.doodle.meetingscheduler.exceptions.InvalidRequestException;
import com.doodle.meetingscheduler.exceptions.MeetingConflictException;
import com.doodle.meetingscheduler.exceptions.SlotNotFoundException;
import com.doodle.meetingscheduler.service.TimeSlotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TimeSlotController.class)
class TimeSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeSlotService slotService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- CREATE SLOT ---
    @Test
    void shouldCreateSlot() throws Exception {
        TimeSlot slot = new TimeSlot();
        slot.setId(1L);

        Mockito.when(slotService.createSlot(eq(1L), eq("2025-10-01T10:00"), eq("2025-10-01T11:00")))
                .thenReturn(slot);

        mockMvc.perform(post("/slots/users/1")
                        .param("start", "2025-10-01T10:00")
                        .param("end", "2025-10-01T11:00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    // --- GET SLOTS FOR USER ---
    @Test
    void shouldGetSlotsForUser() throws Exception {
        TimeSlotDTO slotDto = new TimeSlotDTO();
        slotDto.setId(1L);

        Mockito.when(slotService.getSlotsForUser(eq(1L), any(), any(), any(), any(), any()))
                .thenReturn(List.of(slotDto));

        mockMvc.perform(get("/slots/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void shouldReturn404WhenUserNotFoundInGetSlots() throws Exception {
        Mockito.when(slotService.getSlotsForUser(eq(99L), any(), any(), any(), any(), any()))
                .thenThrow(new InvalidRequestException("User not found"));

        mockMvc.perform(get("/slots/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    // --- GET SLOT BY ID ---
    @Test
    void shouldGetSlotById() throws Exception {
        TimeSlot slot = new TimeSlot();
        slot.setId(5L);

        Mockito.when(slotService.getTimeSlotById(5L)).thenReturn(slot);

        mockMvc.perform(get("/slots/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));
    }

    @Test
    void shouldReturn404WhenSlotNotFound() throws Exception {
        Mockito.when(slotService.getTimeSlotById(99L))
                .thenThrow(new SlotNotFoundException("Slot not found"));

        mockMvc.perform(get("/slots/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Slot not found"));
    }

    // --- UPDATE SLOT ---
    @Test
    void shouldUpdateSlot() throws Exception {

        TimeSlot updated = new TimeSlot();
        updated.setId(7L);

        UpdateSlotRequest req = new UpdateSlotRequest();
        req.setStart("2025-10-01T12:00");
        req.setEnd("2025-10-01T13:00");
        req.setStatus("FREE");

        Mockito.when(slotService.updateSlot(eq(7L), eq("2025-10-01T12:00"), eq("2025-10-01T13:00"), eq("FREE")))
                .thenReturn(updated);

        mockMvc.perform(put("/slots/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentSlot() throws Exception {
        UpdateSlotRequest req = new UpdateSlotRequest();
        req.setStart("2025-10-01T12:00");
        req.setEnd("2025-10-01T13:00");
        req.setStatus("FREE");

        Mockito.when(slotService.updateSlot(eq(99L), any(), any(), any()))
                .thenThrow(new SlotNotFoundException("Slot not found"));

        mockMvc.perform(put("/slots/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Slot not found"));
    }

    // --- DELETE SLOT ---
    @Test
    void shouldDeleteSlot() throws Exception {
        mockMvc.perform(delete("/slots/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentSlot() throws Exception {
        Mockito.doThrow(new SlotNotFoundException("Slot not found")).when(slotService).deleteSlot(99L);

        mockMvc.perform(delete("/slots/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Slot not found"));
    }

    @Test
    void shouldReturn409WhenDeletingBookedSlot() throws Exception {
        Mockito.doThrow(new MeetingConflictException("Slot already booked"))
                .when(slotService).deleteSlot(11L);

        mockMvc.perform(delete("/slots/11"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Slot already booked"));
    }
}
