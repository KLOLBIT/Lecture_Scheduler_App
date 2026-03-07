package services.scheduling;

import data.Lecture;
import data.exceptions.ScheduleConflictException;

import java.time.LocalDate;
import java.util.*;

public class SchedulingService {
    private final ArrayList<Lecture> lectures = new ArrayList<Lecture>();

    public void addLecture(Lecture lecture) throws ScheduleConflictException {
        validateLecture(lecture);

        for (Lecture existing : lectures) {
            if (lecture.hasRoomConflictWith(existing)) {
                throw new ScheduleConflictException("Room " + lecture.roomNumber + " is occupied at " + lecture.timeSlot + " on " + lecture.date + ".");
            }

            if (lecture.isInSameTimeSlot(existing)) {
                throw new ScheduleConflictException("Course already has another lecture scheduled at " + lecture.timeSlot + " on " + lecture.date + ".");
            }
        }

        lectures.add(lecture);
    }

    public Lecture removeLecture(LocalDate date, String timeSlot) {
        for (int i = 0; i < lectures.size(); i++) {
            var lecture = lectures.get(i);

            if (lecture.date.equals(date) && lecture.timeSlot.equals(timeSlot)) {
                lectures.remove(i);
                return lecture;
            }
        }

        return null;
    }

    public List<Lecture> getSchedule() {
        var schedule = new ArrayList<Lecture>(lectures);
        schedule.sort(Comparator.comparing((Lecture lecture) -> lecture.date).thenComparing(lecture -> lecture.timeSlot));
        return schedule;
    }

    private void validateLecture(Lecture lecture) {
        if (lecture == null) {
            throw new IllegalArgumentException("Lecture is missing");
        }
        if (lecture.date == null) {
            throw new IllegalArgumentException("Date is missing");
        }
        if (lecture.timeSlot == null) {
            throw new IllegalArgumentException("Time slot is missing");
        }
        if (lecture.roomNumber == null) {
            throw new IllegalArgumentException("Room number is missing");
        }
        if (lecture.moduleName == null) {
            throw new IllegalArgumentException("Module name is missing");
        }

        var moduleNames = new HashSet<>();
        for (Lecture existing : lectures) {
            moduleNames.add(existing.moduleName);
        }

        moduleNames.add(lecture.moduleName);

        if (moduleNames.size() > 5) {
            throw new IllegalArgumentException("5 modules are supported for this course");
        }
    }
}
