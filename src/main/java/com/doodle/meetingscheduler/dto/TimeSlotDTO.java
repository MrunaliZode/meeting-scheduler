package com.doodle.meetingscheduler.dto;

import com.doodle.meetingscheduler.data.SlotStatus;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class TimeSlotDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SlotStatus status;
    private Long userId;
}

