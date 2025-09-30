package com.doodle.meetingscheduler.exceptions;

/**
 * This exception is thrown when the meeting is not found
 */
public class MeetingNotFoundException extends RuntimeException {

    public MeetingNotFoundException(String message) {
        super(message);
    }
}
