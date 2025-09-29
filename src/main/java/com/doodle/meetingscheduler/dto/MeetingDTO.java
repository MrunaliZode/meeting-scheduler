package com.doodle.meetingscheduler.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import java.util.Set;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MeetingDTO {
    private Long id;
    private String title;
    private String description;
    private Long slotId;
    private Set<Long> participantIds;
}
