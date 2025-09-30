package com.doodle.meetingscheduler.utils;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.exceptions.MeetingConflictException;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlotsHelperTest {

    @Mock
    private TimeSlotRepository slotRepository;

    @InjectMocks
    private SlotsHelper slotsHelper;

    private User user1;
    private User user2;
    private TimeSlot slot1U1;
    private TimeSlot slot2U1;
    private TimeSlot slot1U2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user1 = new User();
        user1.setId(1L);

        user2 = new User();
        user2.setId(2L);

        LocalDateTime now = LocalDateTime.of(2025, 10, 1, 10, 0);

        slot1U1 = new TimeSlot();
        slot1U1.setId(100L);
        slot1U1.setUser(user1);
        slot1U1.setStartTime(now);
        slot1U1.setEndTime(now.plusHours(1));
        slot1U1.setStatus(SlotStatus.FREE);

        slot2U1 = new TimeSlot();
        slot2U1.setId(101L);
        slot2U1.setUser(user1);
        slot2U1.setStartTime(now.plusHours(1));
        slot2U1.setEndTime(now.plusHours(2));
        slot2U1.setStatus(SlotStatus.FREE);

        slot1U2 = new TimeSlot();
        slot1U2.setId(200L);
        slot1U2.setUser(user2);
        slot1U2.setStartTime(now);
        slot1U2.setEndTime(now.plusHours(1));
        slot1U2.setStatus(SlotStatus.FREE);
    }

    // ---- getAggregatedSlots ----
    @Test
    void getAggregatedSlots_returnsSlotsFromRepository() {
        when(slotRepository.findByUserIdsAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
                anyList(), any(), any(), eq(SlotStatus.FREE)))
                .thenReturn(List.of(slot1U1, slot2U1));

        List<TimeSlot> result = slotsHelper.getAggregatedSlots(
                List.of(1L), "2025-10-01T10:00:00", "2025-10-01T12:00:00", SlotStatus.FREE);

        assertThat(result).containsExactly(slot1U1, slot2U1);
    }

    @Test
    void getAggregatedSlots_emptyResult() {
        when(slotRepository.findByUserIdsAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
                anyList(), any(), any(), eq(SlotStatus.FREE)))
                .thenReturn(Collections.emptyList());

        List<TimeSlot> result = slotsHelper.getAggregatedSlots(
                List.of(1L), "2025-10-01T10:00:00", "2025-10-01T12:00:00", SlotStatus.FREE);

        assertThat(result).isEmpty();
    }

    // ---- getCommonSlots ----
    @Test
    void getCommonSlots_successIntersection() {
        // Both users have the same slot [10:00 - 11:00]
        when(slotRepository.findByUserIdsAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
                anyList(), any(), any(), eq(SlotStatus.FREE)))
                .thenReturn(List.of(slot1U1, slot1U2));

        List<TimeSlot> result = slotsHelper.getCommonSlots(
                List.of(1L, 2L), "2025-10-01T10:00:00", "2025-10-01T12:00:00", SlotStatus.FREE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(slot1U1.getStartTime());
    }

    @Test
    void getCommonSlots_partialOverlapButNoExactMatch_returnsEmpty() {
        // User1 has [10:00-11:00], User2 has [10:30-11:30] → no exact match
        slot1U2.setStartTime(slot1U1.getStartTime().plusMinutes(30));
        slot1U2.setEndTime(slot1U1.getEndTime().plusMinutes(30));

        when(slotRepository.findByUserIdsAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
                anyList(), any(), any(), eq(SlotStatus.FREE)))
                .thenReturn(List.of(slot1U1, slot1U2));

        List<TimeSlot> result = slotsHelper.getCommonSlots(
                List.of(1L, 2L), "2025-10-01T10:00:00", "2025-10-01T12:00:00", SlotStatus.FREE);

        assertThat(result).isEmpty();
    }

    @Test
    void getCommonSlots_someUserHasNoAvailability_throwsConflict() {
        // Only user1 has slots, user2 has none
        when(slotRepository.findByUserIdsAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
                anyList(), any(), any(), eq(SlotStatus.FREE)))
                .thenReturn(List.of(slot1U1, slot2U1));

        assertThatThrownBy(() ->
                slotsHelper.getCommonSlots(List.of(1L, 2L), "2025-10-01T10:00:00", "2025-10-01T12:00:00", SlotStatus.FREE))
                .isInstanceOf(MeetingConflictException.class)
                .hasMessageContaining("Some users have no availability");
    }

    @Test
    void getCommonSlots_noUsersProvided_returnsEmpty() {
        when(slotRepository.findByUserIdsAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
                anyList(), any(), any(), eq(SlotStatus.FREE)))
                .thenReturn(Collections.emptyList());

        List<TimeSlot> result = slotsHelper.getCommonSlots(Collections.emptyList(),
                "2025-10-01T10:00:00", "2025-10-01T12:00:00", SlotStatus.FREE);

        assertThat(result).isEmpty();
    }
}
