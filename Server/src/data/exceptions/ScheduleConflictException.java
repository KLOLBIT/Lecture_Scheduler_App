package data.exceptions;

/**
 * Exception thrown when a scheduling conflict occurs during timetable generation.
 */
public class ScheduleConflictException extends Exception {

    /**
     * Creates a schedule conflict exception with the specified message.
     *
     * @param message the detail message.
     */
    public ScheduleConflictException(String message) {
        super(message);
    }

    /**
     * Creates a schedule conflict exception with the specified message and cause.
     *
     * @param message the detail message.
     * @param cause the cause of the exception.
     */
    public ScheduleConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
