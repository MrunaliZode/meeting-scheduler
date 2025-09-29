package com.doodle.meetingscheduler.exceptions;

public class MeetingConflictException extends RuntimeException {

    public MeetingConflictException(String message) {
        super(message);
    }
}
