package com.doodle.meetingscheduler.exceptions;

/**
 * This exception is thrown when meeting conflicts occur. E.g.:
 * 1. No common available slots between users
 * 2. User is busy or booked at the scheduled time
 * 3. The status of time slot is anything else other than free while updating the status
 */
public class MeetingConflictException extends RuntimeException {

    public MeetingConflictException(String message) {
        super(message);
    }
}
