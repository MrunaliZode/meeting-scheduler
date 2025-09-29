package com.doodle.meetingscheduler.service;

import com.doodle.meetingscheduler.data.Meeting;

import java.util.List;

public interface MeetingService {
    Meeting bookMeeting(Long slotId, String title, String description, List<Long> participantIds);
    List<Meeting> getMeetingsForUser(Long userId, String from, String to);
    Meeting getMeeting(Long meetingId);
    Meeting updateMeeting(Long meetingId, String title, String description);
    void deleteMeeting(Long meetingId);
}

