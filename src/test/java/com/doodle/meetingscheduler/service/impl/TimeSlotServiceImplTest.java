package com.doodle.meetingscheduler.service.impl;

class TimeSlotServiceImplTest {

//    private TimeSlotRepository slotRepository;
//    private UserRepository userRepository;
//    private CommonSlotHelper commonSlotHelper;
//    private MeterRegistry meterRegistry;
//    private Counter counterMock;
//    private TimeSlotServiceImpl service;
//
//    @BeforeEach
//    void setup() {
//        slotRepository = mock(TimeSlotRepository.class);
//        userRepository = mock(UserRepository.class);
//        commonSlotHelper = mock(CommonSlotHelper.class);
//        meterRegistry = mock(MeterRegistry.class);
//        counterMock = mock(Counter.class);
//
//        when(meterRegistry.counter(anyString())).thenReturn(counterMock);
//
//        service = new TimeSlotServiceImpl(slotRepository, userRepository, commonSlotHelper, meterRegistry);
//    }
//
//    // ---------- createSlot ----------
//    @Test
//    void createSlot_success() {
//        User user = new User();
//        user.setId(1L);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(slotRepository.save(any())).thenAnswer(i -> i.getArgument(0));
//
//        TimeSlot result = service.createSlot(1L, "2025-09-30T10:00", "2025-09-30T11:00");
//
//        assertNotNull(result);
//        assertEquals(SlotStatus.FREE, result.getStatus());
//        verify(counterMock).increment();
//    }
//
//    @Test
//    void createSlot_userNotFound() {
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(InvalidRequestException.class,
//                () -> service.createSlot(1L, "2025-09-30T10:00", "2025-09-30T11:00"));
//    }
//
//    @Test
//    void createSlot_invalidTimeRange() {
//        User user = new User();
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        assertThrows(InvalidRequestException.class,
//                () -> service.createSlot(1L, "2025-09-30T11:00", "2025-09-30T10:00"));
//    }
//
//    // ---------- updateSlot ----------
//    @Test
//    void updateSlot_success_updateTimeAndStatus() {
//        TimeSlot slot = new TimeSlot();
//        slot.setId(1L);
//        slot.setStatus(SlotStatus.FREE);
//        slot.setStartTime(LocalDateTime.of(2025, 9, 30, 10, 0));
//        slot.setEndTime(LocalDateTime.of(2025, 9, 30, 11, 0));
//
//        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
//        when(slotRepository.save(any())).thenAnswer(i -> i.getArgument(0));
//
//        TimeSlot updated = service.updateSlot(1L, "2025-09-30T12:00", "2025-09-30T13:00", "BUSY");
//
//        assertEquals(SlotStatus.BUSY, updated.getStatus());
//        assertEquals(LocalDateTime.of(2025, 9, 30, 12, 0), updated.getStartTime());
//    }
//
//    @Test
//    void updateSlot_slotNotFound() {
//        when(slotRepository.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(SlotNotFoundException.class,
//                () -> service.updateSlot(1L, "2025-09-30T10:00", "2025-09-30T11:00", "FREE"));
//    }
//
//    @Test
//    void updateSlot_conflictWhenBooked() {
//        TimeSlot slot = new TimeSlot();
//        slot.setStatus(SlotStatus.BOOKED);
//        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
//        assertThrows(MeetingConflictException.class,
//                () -> service.updateSlot(1L, "2025-09-30T12:00", "2025-09-30T13:00", "FREE"));
//    }
//
//    @Test
//    void updateSlot_invalidTimeRange() {
//        TimeSlot slot = new TimeSlot();
//        slot.setStatus(SlotStatus.FREE);
//        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
//        assertThrows(InvalidRequestException.class,
//                () -> service.updateSlot(1L, "2025-09-30T13:00", "2025-09-30T12:00", "FREE"));
//    }
//
//    // ---------- deleteSlot ----------
//    @Test
//    void deleteSlot_success() {
//        TimeSlot slot = new TimeSlot();
//        slot.setId(1L);
//        slot.setStatus(SlotStatus.FREE);
//
//        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
//
//        service.deleteSlot(1L);
//
//        verify(slotRepository).delete(slot);
//        verify(counterMock).increment();
//    }
//
//    @Test
//    void deleteSlot_notFound() {
//        when(slotRepository.findById(1L)).thenReturn(Optional.empty());
//        assertThrows(SlotNotFoundException.class, () -> service.deleteSlot(1L));
//    }
//
//    @Test
//    void deleteSlot_conflictWhenBooked() {
//        TimeSlot slot = new TimeSlot();
//        slot.setStatus(SlotStatus.BOOKED);
//        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
//        assertThrows(MeetingConflictException.class, () -> service.deleteSlot(1L));
//    }
//
//    // ---------- getAggregatedFreeSlots ----------
//    @Test
//    void getAggregatedFreeSlots_allUsersFree() {
//        LocalDateTime start = LocalDateTime.of(2025, 9, 30, 10, 0);
//        LocalDateTime end = LocalDateTime.of(2025, 9, 30, 11, 0);
//
//        TimeSlot slot1 = new TimeSlot();
//        slot1.setStartTime(start);
//        slot1.setEndTime(end);
//        slot1.setStatus(SlotStatus.FREE);
//
//        TimeSlot slot2 = new TimeSlot();
//        slot2.setStartTime(start);
//        slot2.setEndTime(end);
//        slot2.setStatus(SlotStatus.FREE);
//
//        when(slotRepository.findSlotsInRange(anyList(), any(), any()))
//                .thenReturn(List.of(slot1, slot2));
//
//        List<TimeInterval> intervals = service.getAggregatedFreeSlots(List.of(1L, 2L),
//                "2025-09-30T09:00", "2025-09-30T12:00");
//
//        assertEquals(1, intervals.size());
//        assertEquals(start, intervals.get(0).start());
//        assertEquals(end, intervals.get(0).end());
//    }
//
//    @Test
//    void getAggregatedFreeSlots_noCommonSlot() {
//        TimeSlot slot1 = new TimeSlot();
//        slot1.setStartTime(LocalDateTime.of(2025, 9, 30, 10, 0));
//        slot1.setEndTime(LocalDateTime.of(2025, 9, 30, 11, 0));
//        slot1.setStatus(SlotStatus.FREE);
//
//        TimeSlot slot2 = new TimeSlot();
//        slot2.setStartTime(LocalDateTime.of(2025, 9, 30, 12, 0));
//        slot2.setEndTime(LocalDateTime.of(2025, 9, 30, 13, 0));
//        slot2.setStatus(SlotStatus.FREE);
//
//        when(slotRepository.findSlotsInRange(anyList(), any(), any()))
//                .thenReturn(List.of(slot1, slot2));
//
//        List<TimeInterval> intervals = service.getAggregatedFreeSlots(List.of(1L, 2L),
//                "2025-09-30T09:00", "2025-09-30T14:00");
//
//        assertTrue(intervals.isEmpty());
//    }
}
