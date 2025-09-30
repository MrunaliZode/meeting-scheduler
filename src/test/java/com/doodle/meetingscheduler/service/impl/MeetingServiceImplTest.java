package com.doodle.meetingscheduler.service.impl;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.exceptions.InvalidRequestException;
import com.doodle.meetingscheduler.exceptions.MeetingConflictException;
import com.doodle.meetingscheduler.exceptions.MeetingNotFoundException;
import com.doodle.meetingscheduler.repository.MeetingRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.step.StepCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceImplTest {

//    @Mock
//    private MeetingRepository meetingRepository;
//    @Mock
//    private TimeSlotRepository slotRepository;
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private MeterRegistry meterRegistry;
//    @Mock
//    private Counter counter;
//
//    @InjectMocks
//    private MeetingServiceImpl meetingService;
//
//    private User user;
//    private TimeSlot slot;
//
//    @BeforeEach
//    void setup() {
//        user = new User();
//        user.setId(1L);
//        user.setName("Alice");
//
//        slot = new TimeSlot();
//        slot.setId(10L);
//        slot.setStartTime(LocalDateTime.now());
//        slot.setEndTime(LocalDateTime.now().plusHours(1));
//        slot.setStatus(SlotStatus.FREE);
//        slot.setUser(user);
//        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
//    }
//
//    @Test
//    void bookMeeting_success() {
//        when(userRepository.findAllById(List.of(1L)))
//                .thenReturn(List.of(user));
//        when(slotRepository.findFreeSlotsForUserInRange(anyLong(), any(), any()))
//                .thenReturn(List.of(slot));
//        when(meetingRepository.findOverlappingMeetings(anyList(), any(), any()))
//                .thenReturn(List.of());
//        when(slotRepository.updateStatusIfMatches(eq(slot.getId()), eq(SlotStatus.FREE), eq(SlotStatus.BOOKED)))
//                .thenReturn(1);
//        when(meetingRepository.save(any(Meeting.class)))
//                .thenAnswer(inv -> inv.getArgument(0));
//
//        Meeting meeting = meetingService.bookMeeting(
//                1L, "Test Meeting", "Desc", List.of(1L),
//                LocalDateTime.now().toString(),
//                LocalDateTime.now().plusHours(1).toString()
//        );
//
//        assertEquals("Test Meeting", meeting.getTitle());
////        assertEquals(slot.getStartTime(), meeting.getStart());
////        assertEquals(slot.getEndTime(), meeting.getEnd());
//        assertTrue(meeting.getParticipants().contains(user));
//    }
//
//    @Test
//    void bookMeeting_emptyTitle_throws() {
//        assertThrows(InvalidRequestException.class,
//                () -> meetingService.bookMeeting(1L, " ", "desc", List.of(1L), "2025-09-30T10:00", "2025-09-30T11:00"));
//    }
//
//    @Test
//    void bookMeeting_noParticipants_throws() {
//        assertThrows(InvalidRequestException.class,
//                () -> meetingService.bookMeeting(1L, "Title", "desc", List.of(), "2025-09-30T10:00", "2025-09-30T11:00"));
//    }
//
//    @Test
//    void bookMeeting_invalidRange_throws() {
//        assertThrows(InvalidRequestException.class,
//                () -> meetingService.bookMeeting(1L, "Title", "desc", List.of(1L), "2025-09-30T12:00", "2025-09-30T10:00"));
//    }
//
//    @Test
//    void bookMeeting_invalidParticipants_throws() {
//        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of());
//        assertThrows(InvalidRequestException.class,
//                () -> meetingService.bookMeeting(1L, "Title", "desc", List.of(1L), "2025-09-30T10:00", "2025-09-30T11:00"));
//    }
//
//    @Test
//    void bookMeeting_noFreeSlots_throws() {
//        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(user));
//        when(slotRepository.findFreeSlotsForUserInRange(anyLong(), any(), any())).thenReturn(List.of());
//        assertThrows(MeetingConflictException.class,
//                () -> meetingService.bookMeeting(1L, "Title", "desc", List.of(1L), "2025-09-30T10:00", "2025-09-30T11:00"));
//    }
//
//    @Test
//    void bookMeeting_conflictingMeetings_throws() {
//        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(user));
//        when(slotRepository.findFreeSlotsForUserInRange(anyLong(), any(), any())).thenReturn(List.of(slot));
//        when(meetingRepository.findOverlappingMeetings(anyList(), any(), any())).thenReturn(List.of(new Meeting()));
//
//        assertThrows(InvalidRequestException.class,
//                () -> meetingService.bookMeeting(1L, "Title", "desc", List.of(1L), "2025-09-30T10:00", "2025-09-30T11:00"));
//    }
//
//    @Test
//    void bookMeeting_slotUpdateFails_throws() {
//        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(user));
//        when(slotRepository.findFreeSlotsForUserInRange(anyLong(), any(), any())).thenReturn(List.of(slot));
//        when(meetingRepository.findOverlappingMeetings(anyList(), any(), any())).thenReturn(List.of());
//        when(slotRepository.updateStatusIfMatches(anyLong(), any(), any())).thenReturn(0);
//
//        assertThrows(MeetingConflictException.class,
//                () -> meetingService.bookMeeting(1L, "Title", "desc", List.of(1L), "2025-09-30T10:00", "2025-09-30T11:00"));
//    }
//
//    @Test
//    void getMeeting_found() {
//        Meeting m = new Meeting();
//        m.setId(100L);
//        when(meetingRepository.findById(100L)).thenReturn(Optional.of(m));
//        assertEquals(m, meetingService.getMeeting(100L));
//    }
//
//    @Test
//    void getMeeting_notFound_throws() {
//        when(meetingRepository.findById(100L)).thenReturn(Optional.empty());
//        assertThrows(MeetingNotFoundException.class, () -> meetingService.getMeeting(100L));
//    }
//
//    @Test
//    void updateMeeting_updatesFields() {
//        Meeting m = new Meeting();
//        m.setId(100L);
//        m.setTitle("Old");
//        m.setDescription("Old desc");
//
//        when(meetingRepository.findById(100L)).thenReturn(Optional.of(m));
//        when(meetingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        Meeting updated = meetingService.updateMeeting(100L, "New", null);
//        assertEquals("New", updated.getTitle());
//        assertEquals("Old desc", updated.getDescription());
//    }
//
//    @Test
//    void deleteMeeting_success() {
//        Meeting m = new Meeting();
//        m.setId(100L);
////        m.setStart(slot.getStartTime());
////        m.setEnd(slot.getEndTime());
//
//        when(meetingRepository.findById(100L)).thenReturn(Optional.of(m));
//
//        meetingService.deleteMeeting(100L);
//
//        assertEquals(SlotStatus.FREE, slot.getStatus());
//        verify(slotRepository).save(slot);
//        verify(meetingRepository).delete(m);
//    }
}
