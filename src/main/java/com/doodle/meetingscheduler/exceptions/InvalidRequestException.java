package com.doodle.meetingscheduler.exceptions;

/**
 * This exception is thrown when the input request parameters are not valid
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
