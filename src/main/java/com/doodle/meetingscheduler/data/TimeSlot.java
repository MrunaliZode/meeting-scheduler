package com.doodle.meetingscheduler.data;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "time_slots", indexes = {
        @Index(name = "idx_slot_user_time", columnList = "user_id,start_time,end_time"),
        @Index(name = "idx_slot_status", columnList = "status")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private SlotStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Meeting meeting;

}

