package com.doodle.meetingscheduler.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateMeetingRequest {

    private Long slotId;
    private String title;
    private String description;
    private List<Long> participantIds;
}
