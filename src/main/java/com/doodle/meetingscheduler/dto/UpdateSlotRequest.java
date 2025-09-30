package com.doodle.meetingscheduler.dto;

import com.doodle.meetingscheduler.utils.Utility;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSlotRequest {

    @NotNull(message = "Start time cannot be null")
    @NotEmpty(message = "Start time cannot be empty")
    private String start;
    @NotNull(message = "End time cannot be null")
    @NotEmpty(message = "End time cannot be empty")
    private String end;
    @NotNull(message = "Status cannot be null")
    @NotEmpty(message = "Status cannot be empty")
    private String status;

    @AssertTrue(message = "End time must be after start time")
    public boolean isValidInterval() {
        return start != null && end != null && Utility.toLocalDateTime(end).isAfter(Utility.toLocalDateTime(start));
    }
}
