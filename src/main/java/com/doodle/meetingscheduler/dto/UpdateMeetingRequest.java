package com.doodle.meetingscheduler.dto;

import lombok.Data;

@Data
public class UpdateMeetingRequest {

    private String title;
    private String description;
}
