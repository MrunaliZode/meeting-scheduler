package com.doodle.meetingscheduler.service.impl;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.exceptions.InvalidRequestException;
import com.doodle.meetingscheduler.exceptions.MeetingConflictException;
import com.doodle.meetingscheduler.exceptions.SlotNotFoundException;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import com.doodle.meetingscheduler.utils.SlotsHelper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TimeSlotServiceImplTest {

    @Mock private TimeSlotRepository slotRepository;
    @Mock private UserRepository userRepository;
    @Mock private SlotsHelper slotsHelper;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter counter;

    @InjectMocks private TimeSlotServiceImpl timeSlotService;

    private User user;
    private TimeSlot slot;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(meterRegistry.counter(anyString())).thenReturn(counter);

        user = new User();
        user.setId(1L);

        slot = new TimeSlot();
        slot.setId(10L);
        slot.setUser(user);
        slot.setStartTime(LocalDateTime.now());
        slot.setEndTime(LocalDateTime.now().plusHours(1));
        slot.setStatus(SlotStatus.FREE);
    }

    // ---- createSlot ----
    @Test
    void createSlot_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(slotRepository.save(any(TimeSlot.class))).thenReturn(slot);

        TimeSlot result = timeSlotService.createSlot(1L, "2025-10-01T10:00:00", "2025-10-01T11:00:00");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SlotStatus.FREE);
        verify(slotRepository).save(any(TimeSlot.class));
    }

    @Test
    void createSlot_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                timeSlotService.createSlot(1L, "2025-10-01T10:00:00", "2025-10-01T11:00:00"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createSlot_invalidRange() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                timeSlotService.createSlot(1L, "2025-10-01T12:00:00", "2025-10-01T11:00:00"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid time range");
    }

    // ---- updateSlot ----
    @Test
    void updateSlot_success_changeTimeAndStatus() {
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(TimeSlot.class))).thenReturn(slot);

        TimeSlot result = timeSlotService.updateSlot(
                10L, "2025-10-01T08:00:00", "2025-10-01T09:00:00", "BOOKED");

        assertThat(result.getStatus()).isEqualTo(SlotStatus.BOOKED);
        assertThat(result.getStartTime()).isBefore(result.getEndTime());
        verify(slotRepository).save(slot);
    }

    @Test
    void updateSlot_slotNotFound() {
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                timeSlotService.updateSlot(99L, "2025-10-01T10:00:00", "2025-10-01T11:00:00", "FREE"))
                .isInstanceOf(SlotNotFoundException.class);
    }

    @Test
    void updateSlot_slotAlreadyBooked() {
        slot.setStatus(SlotStatus.BOOKED);
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() ->
                timeSlotService.updateSlot(10L, "2025-10-01T10:00:00", "2025-10-01T11:00:00", "FREE"))
                .isInstanceOf(MeetingConflictException.class)
                .hasMessageContaining("Slot is already booked.");
    }

    @Test
    void updateSlot_invalidNewRange() {
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() ->
                timeSlotService.updateSlot(10L, "2025-10-01T12:00:00", "2025-10-01T11:00:00", "FREE"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid time range");
    }

    // ---- getTimeSlotById ----
    @Test
    void getTimeSlotById_found() {
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));

        TimeSlot result = timeSlotService.getTimeSlotById(10L);
        assertThat(result).isEqualTo(slot);
    }

    @Test
    void getTimeSlotById_notFound() {
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeSlotService.getTimeSlotById(99L))
                .isInstanceOf(SlotNotFoundException.class);
    }

    // ---- getCommonFreeSlots ----
    @Test
    void getCommonFreeSlots_success() {
        when(userRepository.findAllById(anyList())).thenReturn(List.of(user));
        when(slotsHelper.getCommonSlots(anyList(), any(), any(), eq(SlotStatus.FREE)))
                .thenReturn(List.of(slot));

        List<TimeSlot> result = timeSlotService.getCommonFreeSlots(List.of(1L), "2025-10-01T10:00:00", "2025-10-01T11:00:00");

        assertThat(result).hasSize(1);
    }

    @Test
    void getCommonFreeSlots_invalidRange() {
        assertThatThrownBy(() ->
                timeSlotService.getCommonFreeSlots(List.of(1L), "2025-10-01T12:00:00", "2025-10-01T11:00:00"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void getCommonFreeSlots_userNotFound() {
        when(userRepository.findAllById(List.of(1L))).thenReturn(Collections.emptyList());

        assertThatThrownBy(() ->
                timeSlotService.getCommonFreeSlots(List.of(1L), "2025-10-01T10:00:00", "2025-10-01T11:00:00"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void getCommonFreeSlots_noCommonSlots() {
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(user));
        when(slotsHelper.getCommonSlots(anyList(), any(), any(), eq(SlotStatus.FREE)))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() ->
                timeSlotService.getCommonFreeSlots(List.of(1L), "2025-10-01T10:00:00", "2025-10-01T11:00:00"))
                .isInstanceOf(MeetingConflictException.class);
    }

    // ---- deleteSlot ----
    @Test
    void deleteSlot_success() {
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));

        timeSlotService.deleteSlot(10L);

        verify(slotRepository).delete(slot);
    }

    @Test
    void deleteSlot_notFound() {
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeSlotService.deleteSlot(99L))
                .isInstanceOf(SlotNotFoundException.class);
    }

    @Test
    void deleteSlot_alreadyBooked() {
        slot.setStatus(SlotStatus.BOOKED);
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> timeSlotService.deleteSlot(10L))
                .isInstanceOf(MeetingConflictException.class);
    }
}
