package com.doodle.meetingscheduler.service.impl;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.exceptions.InvalidRequestException;
import com.doodle.meetingscheduler.exceptions.MeetingConflictException;
import com.doodle.meetingscheduler.exceptions.MeetingNotFoundException;
import com.doodle.meetingscheduler.repository.MeetingRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import com.doodle.meetingscheduler.utils.SlotsHelper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MeetingServiceImplTest {

    private MeetingRepository meetingRepository;
    private UserRepository userRepository;
    private SlotsHelper slotsHelper;
    private MeterRegistry meterRegistry;
    private MeetingServiceImpl meetingService;

    @BeforeEach
    void setUp() {
        meetingRepository = mock(MeetingRepository.class);
        userRepository = mock(UserRepository.class);
        slotsHelper = mock(SlotsHelper.class);
        meterRegistry = mock(MeterRegistry.class);

        Counter dummyCounter = mock(Counter.class);
        when(meterRegistry.counter(anyString())).thenReturn(dummyCounter);

        meetingService = new MeetingServiceImpl(meetingRepository, userRepository, slotsHelper, meterRegistry);
    }

    // --- BOOK MEETING ---
    @Test
    void shouldBookMeetingSuccessfully() {
        Long userId = 1L;
        List<Long> participantsIds = List.of(1L, 2L);
        User u1 = new User(); u1.setId(1L);
        User u2 = new User(); u2.setId(2L);

        TimeSlot slot = new TimeSlot();
        slot.setId(100L);
        slot.setStatus(SlotStatus.FREE);

        when(userRepository.findAllById(participantsIds)).thenReturn(List.of(u1, u2));
        when(slotsHelper.getCommonSlots(participantsIds, "2025-10-01T10:00", "2025-10-01T11:00", SlotStatus.FREE))
                .thenReturn(List.of(slot));
        when(slotsHelper.getAggregatedSlots(participantsIds, "2025-10-01T10:00", "2025-10-01T11:00", SlotStatus.FREE))
                .thenReturn(List.of(slot));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting saved = meetingService.bookMeeting(
                userId, "Title", "Desc", participantsIds,
                "2025-10-01T10:00", "2025-10-01T11:00"
        );

        assertNotNull(saved);
        assertThat(saved.getSlots()).hasSize(1);
        assertThat(saved.getParticipants()).contains(u1, u2);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        assertThat(slot.getMeeting()).isEqualTo(saved);
    }

    @Test
    void shouldThrowInvalidRequestWhenStartAfterEnd() {
        assertThrows(InvalidRequestException.class, () ->
                meetingService.bookMeeting(1L, "t", "d", List.of(1L), "2025-10-02T10:00", "2025-10-01T09:00"));
    }

    @Test
    void shouldThrowInvalidRequestWhenParticipantsInvalid() {
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(new User()));
        assertThrows(InvalidRequestException.class, () ->
                meetingService.bookMeeting(1L, "t", "d", List.of(1L, 2L), "2025-10-01T10:00", "2025-10-01T11:00"));
    }

    @Test
    void shouldThrowMeetingConflictWhenNoCommonSlots() {
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(new User()));
        when(slotsHelper.getCommonSlots(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        assertThrows(MeetingConflictException.class, () ->
                meetingService.bookMeeting(1L, "t", "d", List.of(1L), "2025-10-01T10:00", "2025-10-01T11:00"));
    }

    // --- GET MEETING ---
    @Test
    void shouldGetMeetingSuccessfully() {
        Meeting m = new Meeting();
        m.setId(42L);
        when(meetingRepository.findById(42L)).thenReturn(Optional.of(m));

        Meeting found = meetingService.getMeeting(42L);
        assertThat(found.getId()).isEqualTo(42L);
    }

    @Test
    void shouldThrowWhenMeetingNotFound() {
        when(meetingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(MeetingNotFoundException.class, () -> meetingService.getMeeting(99L));
    }

    // --- DELETE MEETING ---
    @Test
    void shouldDeleteMeetingAndFreeSlots() {
        Meeting m = new Meeting();
        TimeSlot s1 = new TimeSlot(); s1.setStatus(SlotStatus.BOOKED);
        s1.setMeeting(m);
        m.setSlots(Set.of(s1));

        when(meetingRepository.findById(1L)).thenReturn(Optional.of(m));

        meetingService.deleteMeeting(1L);

        assertThat(s1.getStatus()).isEqualTo(SlotStatus.FREE);
        assertThat(s1.getMeeting()).isNull();
        verify(meetingRepository).delete(m);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentMeeting() {
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(MeetingNotFoundException.class, () -> meetingService.deleteMeeting(999L));
    }

    // --- GET MEETINGS FOR USERS ---
    @Test
    void shouldGetMeetingsForUsers() {
        Meeting m1 = new Meeting(); m1.setId(1L);
        when(meetingRepository.findMeetingsByParticipantIdsAndStartTimeGreaterThanEqualsAndEndTimeLessThanEquals(any(), any(), any()))
                .thenReturn(List.of(m1));

        Page<Meeting> page = meetingService.getMeetingsForUsers(List.of(1L), null, null, 0, 10);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(1L);
    }
}
