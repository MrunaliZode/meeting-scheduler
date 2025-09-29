package com.doodle.meetingscheduler.exceptions;

public class MeetingNotFoundException extends RuntimeException {

    public MeetingNotFoundException(String message) {
        super(message);
    }
}
