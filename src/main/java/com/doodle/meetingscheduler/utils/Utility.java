package com.doodle.meetingscheduler.utils;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.data.SlotStatus;
import com.doodle.meetingscheduler.data.TimeSlot;
import com.doodle.meetingscheduler.data.User;
import com.doodle.meetingscheduler.dto.MeetingDTO;
import com.doodle.meetingscheduler.dto.TimeSlotDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class Utility {

    public static LocalDateTime toLocalDateTime(String time) {
        return LocalDateTime.parse(time);
    }

    public static SlotStatus toSlotStatus(String status) {
        return SlotStatus.valueOf(status.toUpperCase());
    }

    public static MeetingDTO toMeetingDTO(Meeting meeting) {
        MeetingDTO dto = new MeetingDTO();
        dto.setId(meeting.getId());
        dto.setTitle(meeting.getTitle());
        dto.setDescription(meeting.getDescription());
        dto.setSlotId(meeting.getSlot().getId());
        dto.setParticipantIds(meeting.getParticipants().stream().map(User::getId).collect(Collectors.toSet()));
        return dto;
    }

    public static List<MeetingDTO> toMeetingDTOList(List<Meeting> meetings) {
        return meetings.stream().map(Utility::toMeetingDTO).collect(Collectors.toList());
    }

    public static TimeSlotDTO toTimeSlotDTO(TimeSlot slot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(slot.getId());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setStatus(slot.getStatus());
        dto.setUserId(slot.getUser().getId());
        return dto;
    }

    public static List<TimeSlotDTO> toTimeSlotDTOList(List<TimeSlot> slots) {
        return slots.stream().map(Utility::toTimeSlotDTO).collect(Collectors.toList());
    }
}
