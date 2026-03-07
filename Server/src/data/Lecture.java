package data;

import java.time.LocalDate;

public class Lecture {
    public LocalDate date;
    public String timeSlot;
    public String roomNumber;
    public String moduleName;

    public Lecture(LocalDate date, String timeSlot, String roomNumber, String moduleName) {
        this.date = date;
        this.timeSlot = timeSlot;
        this.roomNumber = roomNumber;
        this.moduleName = moduleName;
    }

    public boolean isInSameTimeSlot(Lecture other) {
        if (other == null || date == null || timeSlot == null) {
            return false;
        }

        return date.equals(other.date) && timeSlot.equals(other.timeSlot);
    }

    public boolean hasRoomConflictWith(Lecture other) {
        if (other == null || roomNumber == null) {
            return false;
        }

        return isInSameTimeSlot(other) && roomNumber.equals(other.roomNumber);
    }
}
