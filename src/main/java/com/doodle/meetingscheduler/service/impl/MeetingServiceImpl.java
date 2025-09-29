package com.doodle.meetingscheduler.service.impl;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.dto.MeetingDTO;
import com.doodle.meetingscheduler.exceptions.InvalidRequestException;
import com.doodle.meetingscheduler.exceptions.MeetingConflictException;
import com.doodle.meetingscheduler.exceptions.MeetingNotFoundException;
import com.doodle.meetingscheduler.exceptions.SlotNotFoundException;
import com.doodle.meetingscheduler.repository.MeetingRepository;
import com.doodle.meetingscheduler.repository.TimeSlotRepository;
import com.doodle.meetingscheduler.repository.UserRepository;
import com.doodle.meetingscheduler.service.MeetingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final TimeSlotRepository slotRepository;
    private final UserRepository userRepository;

    public MeetingServiceImpl(MeetingRepository meetingRepository, TimeSlotRepository slotRepository, UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Meeting bookMeeting(Long slotId, String title, String description, List<Long> participantIds) {
        TimeSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        if (slot.getStatus() != SlotStatus.FREE) {
            throw new MeetingConflictException("Slot is not free for booking");
        }

        List<Meeting> overlaps = meetingRepository.findOverlappingMeetings(
                participantIds, slot.getStartTime(), slot.getEndTime()
        );

        if (!overlaps.isEmpty()) {
            throw new InvalidRequestException("One or more participants already have overlapping meetings");
        }

        List<User> participants = userRepository.findAllById(participantIds);
        if (participants.isEmpty()) {
            throw new InvalidRequestException("No participants found");
        }

        if (participants.size() != participantIds.size()) {
            throw new InvalidRequestException("Some participant IDs are invalid");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        Meeting meeting = new Meeting();
        meeting.setSlot(slot);
        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setParticipants(new HashSet<>(participants));

        return meetingRepository.save(meeting);
    }

    @Override
    public List<Meeting> getMeetingsForUser(Long userId, LocalDateTime from, LocalDateTime to) {
        return meetingRepository.findByParticipantAndTimeRange(userId, from, to);
    }

    @Override
    public Meeting getMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting not found"));
    }

    @Override
    public Meeting updateMeeting(Long meetingId, String title, String description) {
        Meeting meeting = getMeeting(meetingId);
        meeting.setTitle(title);
        meeting.setDescription(description);
        return meetingRepository.save(meeting);
    }

    @Override
    public void deleteMeeting(Long meetingId) {
        Meeting meeting = getMeeting(meetingId);
        // free the slot
        TimeSlot slot = meeting.getSlot();
        slot.setStatus(SlotStatus.FREE);
        slotRepository.save(slot);

        meetingRepository.delete(meeting);
    }

    @Override
    public MeetingDTO toDTO(Meeting meeting) {
        MeetingDTO dto = new MeetingDTO();
        dto.setId(meeting.getId());
        dto.setTitle(meeting.getTitle());
        dto.setDescription(meeting.getDescription());
        dto.setSlotId(meeting.getSlot().getId());
        dto.setParticipantIds(meeting.getParticipants().stream().map(User::getId).collect(Collectors.toSet()));
        return dto;
    }

    @Override
    public List<MeetingDTO> toDTOList(List<Meeting> meetings) {
        return meetings.stream().map(this::toDTO).collect(Collectors.toList());
    }
}

