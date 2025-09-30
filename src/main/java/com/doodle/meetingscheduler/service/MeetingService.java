package com.doodle.meetingscheduler.service;

import com.doodle.meetingscheduler.data.Meeting;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MeetingService {

    /**
     * Creates a meeting
     *
     * @param userId         meeting creator
     * @param title          meeting title
     * @param description    meeting description
     * @param participantIds participants in the meeting
     * @param from           meeting start time
     * @param to             meeting end time
     * @return created meeting
     */
    Meeting bookMeeting(Long userId, String title, String description, List<Long> participantIds, String from, String to);

    /**
     * Gets the paginated view of meetings for the users within specified time range
     *
     * @param userIds identities of the users
     * @param from    lower bound time search
     * @param to      upper bound time search
     * @param page    page number
     * @param size    size of the page
     * @return the meetings(paginated) for the users bewtween specified range
     */
    Page<Meeting> getMeetingsForUsers(List<Long> userIds, String from, String to, Integer page, Integer size);

    /**
     * Get the meeting
     *
     * @param meetingId identity of the meeting
     * @return meeting
     */
    Meeting getMeeting(Long meetingId);

    /**
     * Deletes the meeting
     *
     * @param meetingId identity of the meeting
     */
    void deleteMeeting(Long meetingId);
}

