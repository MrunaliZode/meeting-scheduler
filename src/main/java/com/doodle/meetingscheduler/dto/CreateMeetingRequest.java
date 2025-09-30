package com.doodle.meetingscheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateMeetingRequest {

    @NotNull
    private Long userId;
    @NotBlank(message = "Title cannot be empty")
    @NotNull(message = "Title cannot be null")
    private String title;
    @NotBlank(message = "Description cannot be empty")
    @NotNull(message = "Description cannot be null")
    private String description;
    @NotNull(message = "Participants cannot be null")
    @NotEmpty(message = "At least one participant is required")
    private List<Long> participantIds;
    @NotNull(message = "Start time is required")
    private String from;
    @NotNull(message = "End time is required")
    private String to;
}
