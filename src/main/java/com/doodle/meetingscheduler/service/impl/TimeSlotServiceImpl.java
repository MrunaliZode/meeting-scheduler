package com.doodle.meetingscheduler.service.impl;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import com.doodle.meetingscheduler.service.TimeSlotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository slotRepository;
    private final UserRepository userRepository;

    public TimeSlotServiceImpl(TimeSlotRepository slotRepository, UserRepository userRepository) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TimeSlot createSlot(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!start.isBefore(end)) throw new RuntimeException("Invalid time range");

        TimeSlot slot = new TimeSlot();
        slot.setUser(user);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setStatus(SlotStatus.FREE);

        return slotRepository.save(slot);
    }

    @Transactional
    @Override
    public TimeSlot updateSlot(Long slotId, LocalDateTime newStart, LocalDateTime newEnd, SlotStatus newStatus) {
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // Update start/end time if provided
        if (newStart != null && newEnd != null) {
            if (!newStart.isBefore(newEnd)) {
                throw new IllegalArgumentException("Invalid time range");
            }
            slot.setStartTime(newStart);
            slot.setEndTime(newEnd);
        }

        // Update status if provided
        if (newStatus != null && !newStatus.equals(slot.getStatus())) {
            if (newStatus == SlotStatus.BUSY) {
                // Reuse markSlotAsBusy logic to handle splitting
                return markSlot(slotId, slot.getStartTime(), slot.getEndTime(), SlotStatus.BUSY);
            } else {
                // Mark FREE and merge with adjacent free slots
                slot.setStatus(SlotStatus.FREE);
                slot = slotRepository.save(slot);
                mergeAdjacentFreeSlots(slot);
            }
        } else {
            slot = slotRepository.save(slot);
        }

        return slot;
    }

    private void mergeAdjacentFreeSlots(TimeSlot slot) {
        List<TimeSlot> freeSlots = slotRepository.findByUserIdAndStatus(slot.getUser().getId(), SlotStatus.FREE);

        for (TimeSlot other : freeSlots) {
            if (other.getId().equals(slot.getId())) continue;

            boolean mergeBefore = other.getEndTime().equals(slot.getStartTime());
            boolean mergeAfter = other.getStartTime().equals(slot.getEndTime());

            if (mergeBefore) {
                slot.setStartTime(other.getStartTime());
                slotRepository.delete(other);
            }
            if (mergeAfter) {
                slot.setEndTime(other.getEndTime());
                slotRepository.delete(other);
            }
        }
        slotRepository.save(slot);
    }

    @Override
    @Transactional
    public TimeSlot markSlot(Long slotId, LocalDateTime start, LocalDateTime end, SlotStatus status) {
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        if (status == SlotStatus.FREE) {
            // Mark as FREE and merge with adjacent free slots
            slot.setStatus(SlotStatus.FREE);

            // Optional: Update times if provided
            if (start != null && end != null) {
                if (!start.isBefore(end)) throw new IllegalArgumentException("Invalid time range");
                slot.setStartTime(start);
                slot.setEndTime(end);
            }

            slot = slotRepository.save(slot);
            mergeAdjacentFreeSlots(slot);

            return slot;
        }

        // For BUSY or BOOKED, keep previous split logic
        if (slot.getStatus() != SlotStatus.FREE) {
            throw new IllegalArgumentException("Cannot mark a non-free slot as BUSY/BOOKED");
        }

        LocalDateTime busyStart = start != null ? start : slot.getStartTime();
        LocalDateTime busyEnd = end != null ? end : slot.getEndTime();

        List<TimeSlot> newSlots = new ArrayList<>();

        // Before busy
        if (busyStart.isAfter(slot.getStartTime())) {
            TimeSlot before = new TimeSlot();
            before.setUser(slot.getUser());
            before.setStartTime(slot.getStartTime());
            before.setEndTime(busyStart);
            before.setStatus(SlotStatus.FREE);
            newSlots.add(before);
        }

        // Busy/Booked slot
        TimeSlot busySlot = new TimeSlot();
        busySlot.setUser(slot.getUser());
        busySlot.setStartTime(busyStart);
        busySlot.setEndTime(busyEnd);
        busySlot.setStatus(status); // BUSY or BOOKED
        newSlots.add(busySlot);

        // After busy
        if (busyEnd.isBefore(slot.getEndTime())) {
            TimeSlot after = new TimeSlot();
            after.setUser(slot.getUser());
            after.setStartTime(busyEnd);
            after.setEndTime(slot.getEndTime());
            after.setStatus(SlotStatus.FREE);
            newSlots.add(after);
        }

        slotRepository.delete(slot);
        slotRepository.saveAll(newSlots);

        return busySlot;
    }


    public List<TimeSlot> getSlotsByStatus(Long userId, LocalDateTime from, LocalDateTime to, SlotStatus status) {
        return slotRepository.findByUserIdAndStatusAndStartTimeBetween(userId, status, from, to);
    }

    @Override
    public List<TimeSlot> getSlotsForUser(Long userId) {
        return slotRepository.findByUserId(userId);
    }

    public void deleteSlot(Long slotId) {
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        slotRepository.delete(slot);
    }

    @Override
    public List<TimeSlot> getAggregatedFreeSlots(List<Long> userIds, LocalDateTime from, LocalDateTime to) {
        // Fetch all FREE slots for each user in the range
        List<TimeSlot> allSlots = slotRepository.findByUserIdInAndStatusAndStartTimeBetween(userIds, SlotStatus.FREE, from, to);

        // Group slots by user
        Map<Long, List<TimeSlot>> slotsByUser = allSlots.stream()
                .collect(Collectors.groupingBy(slot -> slot.getUser().getId()));

        // Merge slots for each user individually
        Map<Long, List<Interval>> mergedByUser = new HashMap<>();
        for (Long userId : userIds) {
            List<TimeSlot> userSlots = slotsByUser.getOrDefault(userId, new ArrayList<>());
            List<Interval> merged = mergeIntervals(userSlots);
            mergedByUser.put(userId, merged);
        }

        // Compute intersection across all users
        List<Interval> commonIntervals = intersectIntervals(new ArrayList<>(mergedByUser.values()));

        // Convert to TimeSlot objects for response
        return commonIntervals.stream()
                .map(i -> {
                    TimeSlot t = new TimeSlot();
                    t.setStartTime(i.start);
                    t.setEndTime(i.end);
                    t.setStatus(SlotStatus.FREE);
                    return t;
                })
                .collect(Collectors.toList());
    }

    // Merge overlapping/adjacent intervals for one user
    private List<Interval> mergeIntervals(List<TimeSlot> slots) {
        List<Interval> intervals = slots.stream()
                .map(s -> new Interval(s.getStartTime(), s.getEndTime()))
                .sorted(Comparator.comparing(i -> i.start))
                .collect(Collectors.toList());

        List<Interval> merged = new ArrayList<>();
        for (Interval curr : intervals) {
            if (merged.isEmpty() || merged.get(merged.size() - 1).end.isBefore(curr.start)) {
                merged.add(curr);
            } else {
                merged.get(merged.size() - 1).end = curr.end.isAfter(merged.get(merged.size() - 1).end) ? curr.end : merged.get(merged.size() - 1).end;
            }
        }
        return merged;
    }

    // Intersect lists of intervals (all users)
    private List<Interval> intersectIntervals(List<List<Interval>> usersIntervals) {
        if (usersIntervals.isEmpty()) return Collections.emptyList();
        List<Interval> result = usersIntervals.get(0);

        for (int i = 1; i < usersIntervals.size(); i++) {
            result = intersectTwo(result, usersIntervals.get(i));
            if (result.isEmpty()) break;
        }
        return result;
    }

    private List<Interval> intersectTwo(List<Interval> a, List<Interval> b) {
        List<Interval> intersection = new ArrayList<>();
        int i = 0, j = 0;

        while (i < a.size() && j < b.size()) {
            LocalDateTime start = a.get(i).start.isAfter(b.get(j).start) ? a.get(i).start : b.get(j).start;
            LocalDateTime end = a.get(i).end.isBefore(b.get(j).end) ? a.get(i).end : b.get(j).end;

            if (!start.isAfter(end)) {
                intersection.add(new Interval(start, end));
            }

            if (a.get(i).end.isBefore(b.get(j).end)) i++;
            else j++;
        }
        return intersection;
    }

    // Helper class
    private static class Interval {
        LocalDateTime start;
        LocalDateTime end;
        Interval(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
}

