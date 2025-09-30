package com.doodle.meetingscheduler.exceptions;

/**
 * This exception is thrown when the time slot is not found
 */
public class SlotNotFoundException extends RuntimeException {

    public SlotNotFoundException(String message) {
        super(message);
    }
}
