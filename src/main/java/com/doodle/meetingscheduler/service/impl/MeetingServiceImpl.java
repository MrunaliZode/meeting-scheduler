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
import com.doodle.meetingscheduler.service.MeetingService;
import com.doodle.meetingscheduler.utils.SlotsHelper;
import com.doodle.meetingscheduler.utils.Utility;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final SlotsHelper slotsHelper;
    private final MeterRegistry meterRegistry;

    public MeetingServiceImpl(MeetingRepository meetingRepository,
                              UserRepository userRepository,
                              SlotsHelper slotsHelper,
                              MeterRegistry meterRegistry) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.slotsHelper = slotsHelper;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public Meeting bookMeeting(Long userId, String title, String description, List<Long> participantIds, String fromStr, String toStr) {

        LocalDateTime from = Utility.toLocalDateTime(fromStr);
        LocalDateTime to = Utility.toLocalDateTime(toStr);

        // validate the range
        if (from.isAfter(to)) throw new InvalidRequestException("'start' must be before 'end'");

        // Fetch participants
        List<User> participants = userRepository.findAllById(participantIds);
        if (participants.size() != participantIds.size()) {
            throw new InvalidRequestException("Some participant IDs are invalid");
        }

        // verify if all participants are free at the specified time
        List<TimeSlot> commonSlots = slotsHelper.getCommonSlots(participantIds, fromStr, toStr, SlotStatus.FREE);

        if (commonSlots.isEmpty()) {
            // there are no common free time within specified range
            meterRegistry.counter("meetings_conflict_total").increment();
            throw new MeetingConflictException("No free slots available for requested duration");
        }

        // gets all free time slots available for the user between specified range
        // since we are querying for only one time slot, no chances of updating invalid entries
        List<TimeSlot> slotsToBook = slotsHelper.getAggregatedSlots(participantIds, fromStr, toStr, SlotStatus.FREE);
        slotsToBook.forEach(slot -> slot.setStatus(SlotStatus.BOOKED));

        // create meeting object
        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setSlots(new HashSet<>(slotsToBook));
        meeting.setParticipants(new HashSet<>(participants));

        meeting.getSlots().forEach(slot -> slot.setMeeting(meeting));

        // save meeting
        Meeting saved = meetingRepository.save(meeting);

        meterRegistry.counter("meetings_booked_total").increment();
        return saved;
    }

    @Override
    public Page<Meeting> getMeetingsForUsers(List<Long> userIds, String from, String to, Integer page, Integer size) {

        // update with default start and end time, if not specified
        LocalDateTime start = (from != null && !from.isEmpty()) ? Utility.toLocalDateTime(from) : LocalDateTime.now();
        LocalDateTime end = (to != null && !to.isEmpty()) ? Utility.toLocalDateTime(to) : LocalDateTime.now().plusMonths(2);
        Pageable pageable = PageRequest.of(page, size);

        // get the meetings
        List<Meeting> meetings = meetingRepository.findMeetingsByParticipantIdsAndStartTimeGreaterThanEqualsAndEndTimeLessThanEquals(userIds, start, end);

        return PageableExecutionUtils.getPage(meetings, pageable, meetings::size);
    }

    @Override
    public Meeting getMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting not found"));
    }

    @Transactional
    @Override
    public void deleteMeeting(Long meetingId) {

        // check for the availability of the meeting in the DB
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("This meeting does not exist."));

        Set<TimeSlot> timeSlotsToFree = meeting.getSlots();

        // free up the slots
        timeSlotsToFree.iterator().forEachRemaining(timeSlot -> {
            timeSlot.setStatus(SlotStatus.FREE);
            timeSlot.setMeeting(null);
        });

        // delete the meeting
        meetingRepository.delete(meeting);

        meterRegistry.counter("meetings_deleted_total").increment();
    }
}
