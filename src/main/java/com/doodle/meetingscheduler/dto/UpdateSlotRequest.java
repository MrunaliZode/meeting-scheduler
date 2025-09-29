package com.doodle.meetingscheduler.dto;

import lombok.Data;

@Data
public class UpdateSlotRequest {

    private Long slotId;
    private String start;
    private String end;
    private String status;
}
