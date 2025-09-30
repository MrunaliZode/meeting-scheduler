package com.doodle.meetingscheduler.controller;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.dto.TimeSlotDTO;
import com.doodle.meetingscheduler.dto.UpdateSlotRequest;
import com.doodle.meetingscheduler.service.TimeSlotService;
import com.doodle.meetingscheduler.utils.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TimeSlotControllerTest {

//    @Mock
//    private TimeSlotService slotService;
//
//    @InjectMocks
//    private TimeSlotController slotController;
//
//    private TimeSlot slot;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        slot = new TimeSlot();
//        slot.setId(1L);
//        slot.setUser(new User(1L, "user1", "user1@doodle.com"));
//        slot.setStartTime(LocalDateTime.now());
//        slot.setEndTime(LocalDateTime.now().plusHours(1));
//        slot.setStatus(SlotStatus.FREE);
//    }
//
//    @Test
//    void testCreateSlot() {
//        when(slotService.createSlot(anyLong(), anyString(), anyString())).thenReturn(slot);
//
//        ResponseEntity<TimeSlotDTO> response = slotController.createSlot(
//                1L, slot.getStartTime().toString(), slot.getEndTime().toString());
//
//        assertEquals(201, response.getStatusCodeValue());
//        assertEquals(slot.getId(), response.getBody().getId());
//
//        verify(slotService, times(1)).createSlot(anyLong(), anyString(), anyString());
//    }
//
//    @Test
//    void testGetSlotsForUser() {
//        TimeSlotDTO dto = Utility.toTimeSlotDTO(slot);
//        when(slotService.getSlotsForUser(anyLong(), any(), any(), any(), any(), any()))
//                .thenReturn(List.of(dto));
//
//        ResponseEntity<List<TimeSlotDTO>> response = slotController.getSlotsForUser(
//                1L, null, null, null, 0, 10);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(1, response.getBody().size());
//
//        verify(slotService, times(1))
//                .getSlotsForUser(anyLong(), any(), any(), any(), any(), any());
//    }
//
//    @Test
//    void testGetAggregatedSlots() {
//        TimeInterval interval = new TimeInterval(slot.getStartTime(), slot.getEndTime());
//        when(slotService.getAggregatedFreeSlots(anyList(), anyString(), anyString()))
//                .thenReturn(List.of(interval));
//
//        ResponseEntity<List<TimeInterval>> response = slotController.getAggregatedSlots(
//                Arrays.asList(1L, 2L), slot.getStartTime().toString(), slot.getEndTime().toString());
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(1, response.getBody().size());
//
//        verify(slotService, times(1))
//                .getAggregatedFreeSlots(anyList(), anyString(), anyString());
//    }
//
//    @Test
//    void testUpdateSlot() {
//        Long slotId = 1L;
//        UpdateSlotRequest request = new UpdateSlotRequest();
//        request.setStart(slot.getStartTime().toString());
//        request.setEnd(slot.getEndTime().toString());
//        request.setStatus("BOOKED");
//
//        slot.setStatus(SlotStatus.BOOKED);
//        when(slotService.updateSlot(anyLong(), any(), any(), anyString())).thenReturn(slot);
//
//        ResponseEntity<TimeSlotDTO> response = slotController.updateSlot(slotId, request);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(SlotStatus.BOOKED, response.getBody().getStatus());
//
//        verify(slotService, times(1))
//                .updateSlot(anyLong(), any(), any(), anyString());
//    }
//
//    @Test
//    void testDeleteSlot() {
//        doNothing().when(slotService).deleteSlot(anyLong());
//
//        ResponseEntity<Void> response = slotController.deleteSlot(1L);
//
//        assertEquals(204, response.getStatusCodeValue());
//
//        verify(slotService, times(1)).deleteSlot(anyLong());
//    }
}
