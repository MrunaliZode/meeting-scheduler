package com.doodle.meetingscheduler.service.impl;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.dto.TimeSlotDTO;
import com.doodle.meetingscheduler.exceptions.InvalidRequestException;
import com.doodle.meetingscheduler.exceptions.MeetingConflictException;
import com.doodle.meetingscheduler.exceptions.SlotNotFoundException;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import com.doodle.meetingscheduler.service.TimeSlotService;
import com.doodle.meetingscheduler.utils.SlotsHelper;
import com.doodle.meetingscheduler.utils.Utility;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository slotRepository;
    private final UserRepository userRepository;
    private final SlotsHelper slotsHelper;
    private final MeterRegistry meterRegistry;

    public TimeSlotServiceImpl(TimeSlotRepository slotRepository, UserRepository userRepository, SlotsHelper slotsHelper, MeterRegistry meterRegistry) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.slotsHelper = slotsHelper;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public TimeSlot createSlot(Long userId, String from, String to) {
        LocalDateTime start = Utility.toLocalDateTime(from);
        LocalDateTime end = Utility.toLocalDateTime(to);
        // find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        // validate the time range
        if (!start.isBefore(end)) throw new InvalidRequestException("Invalid time range");

        // create time slot
        TimeSlot slot = new TimeSlot();
        slot.setUser(user);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setStatus(SlotStatus.FREE);

        // persist
        TimeSlot timeSlot = slotRepository.save(slot);
        meterRegistry.counter("timeslots_created_total").increment();
        return timeSlot;
    }

    @Transactional
    @Override
    public TimeSlot updateSlot(Long slotId, String newStart, String newEnd, String newSlotStatus) {

        LocalDateTime newFrom = Utility.toLocalDateTime(newStart);
        LocalDateTime newTo = Utility.toLocalDateTime(newEnd);
        SlotStatus newStatus = Utility.toSlotStatus(newSlotStatus);

        // find the time slot to update
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        // check if the slot is booked already, status changes not allowed for booked slots
        if (slot.getStatus().equals(SlotStatus.BOOKED)) {
            meterRegistry.counter("timeslots_conflicts_total").increment();
            throw new MeetingConflictException("Slot is already booked.");
        }

        // Update start/end time if provided
        if (newFrom != null && newTo != null) {
            if (!newFrom.isBefore(newTo)) {
                throw new InvalidRequestException("Invalid time range");
            }
            slot.setStartTime(newFrom);
            slot.setEndTime(newTo);
        }

        // Update status if provided
        if (newStatus != null && !newStatus.equals(slot.getStatus())) {
            slot.setStatus(newStatus);
        }
        // persist to the DB
        slot = slotRepository.save(slot);
        meterRegistry.counter("timeslots_updated_total").increment();
        return slot;
    }

    @Override
    public TimeSlot getTimeSlotById(Long slotId) {
        TimeSlot slot = slotRepository.findById(slotId).orElseThrow(
                () -> new SlotNotFoundException("The slot does not exist.")
        );
        return slot;
    }

    @Override
    public List<TimeSlot> getCommonFreeSlots(List<Long> userIds, String fromStr, String toStr) {

        LocalDateTime from = Utility.toLocalDateTime(fromStr);
        LocalDateTime to = Utility.toLocalDateTime(toStr);

        // validate the range
        if (from.isAfter(to)) throw new InvalidRequestException("'start' must be before 'end'");

        // Fetch participants
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new InvalidRequestException("Some participant IDs are invalid");
        }

        // get common availability
        List<TimeSlot> commonSlots = slotsHelper.getCommonSlots(userIds, fromStr, toStr, SlotStatus.FREE);

        if (commonSlots.isEmpty()) {
            meterRegistry.counter("meetings_conflict_total").increment();
            throw new MeetingConflictException("No free slots available for requested duration");
        }

        return commonSlots;
    }

    @Override
    public List<TimeSlotDTO> getSlotsForUser(Long userId, String from, String to, String status, Integer page, Integer size) {
        // Validate from/to rule
        if ((from != null && to == null) || (from == null && to != null)) {
            throw new InvalidRequestException("Both 'from' and 'to' parameters must be provided together.");
        }

        // Parse LocalDateTime safely
        LocalDateTime start = null;
        LocalDateTime end = null;
        try {
            if (from != null) start = Utility.toLocalDateTime(from);
            if (to != null) end = Utility.toLocalDateTime(to);
        } catch (Exception e) {
            throw new InvalidRequestException("Invalid date format. Expected ISO_LOCAL_DATE_TIME.");
        }

        // Parse status safely
        SlotStatus slotStatus = null;
        if (status != null) {
            try {
                slotStatus = Utility.toSlotStatus(status);
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException("Invalid status value: " + status);
            }
        }

        // Case 1: range + status
        if (start != null && end != null && slotStatus != null) {
            return Utility.toTimeSlotDTOList(slotRepository.findByUserIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
                    userId, start, end, slotStatus));
        }

        // Case 2: range only
        if (start != null && end != null) {
            return Utility.toTimeSlotDTOList(slotRepository.findByUserIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(userId, start, end));
        }

        // Case 3: status only
        if (slotStatus != null) {
            return Utility.toTimeSlotDTOList(slotRepository.findByUserIdAndStatus(userId, slotStatus));
        }

        // Case 4: no filters → return all slots
        Pageable pageable = page != null && size != null ? PageRequest.of(page, size) : Pageable.unpaged();
        Page<TimeSlot> timeSlotPage = slotRepository.findByUserId(userId, pageable);
        List<TimeSlotDTO> timeSlots = Utility.toTimeSlotDTOList(timeSlotPage);
        return timeSlots;
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId) {

        // find the slot in the DB
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        // cannot delete a booked slot
        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new MeetingConflictException("Slot already booked and cannot be deleted");
        }
        // delete the slot
        slotRepository.delete(slot);
        meterRegistry.counter("timeslots_deleted_total").increment();
    }

    public List<TimeSlot> getAggregatedFreeSlots(List<Long> userIds,
                                                 String from,
                                                 String to) {

        return slotsHelper.getAggregatedSlots(userIds, from, to, SlotStatus.FREE);
    }

}

