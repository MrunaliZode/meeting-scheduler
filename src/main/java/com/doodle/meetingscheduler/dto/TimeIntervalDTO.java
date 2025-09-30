package com.doodle.meetingscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TimeIntervalDTO {

    private LocalDateTime from;
    private LocalDateTime to;
}
