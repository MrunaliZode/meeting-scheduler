package com.doodle.meetingscheduler.service;

import com.doodle.meetingscheduler.data.Meeting;
import com.doodle.meetingscheduler.dto.MeetingDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingService {
    Meeting bookMeeting(Long slotId, String title, String description, List<Long> participantIds);
    List<Meeting> getMeetingsForUser(Long userId, LocalDateTime from, LocalDateTime to);
    Meeting getMeeting(Long meetingId);
    Meeting updateMeeting(Long meetingId, String title, String description);
    void deleteMeeting(Long meetingId);
    MeetingDTO toDTO(Meeting meeting);
    List<MeetingDTO> toDTOList(List<Meeting> meetings);
}

