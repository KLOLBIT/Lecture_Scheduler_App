package services.scheduling;

import data.Lecture;
import data.WeekSchedule;
import data.exceptions.ScheduleConflictException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class SchedulingService implements ISchedulingService {
    private static final int MAX_MODULES = 5;

    private final ArrayList<Lecture> lectures = new ArrayList<Lecture>();

    @Override
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

    @Override
    public Lecture removeLecture(LocalDate date, String timeSlot) {
        for (int i = 0; i < lectures.size(); i++) {
            Lecture lecture = lectures.get(i);
            if (lecture.date.equals(date) && lecture.timeSlot.equals(timeSlot)) {
                lectures.remove(i);
                return lecture;
            }
        }

        return null;
    }

    @Override
    public WeekSchedule getWeekSchedule() {
        WeekSchedule schedule = new WeekSchedule();
        for (Lecture lecture : lectures) {
            schedule.addLecture(lecture);
        }
        return schedule;
    }

    private void validateLecture(Lecture lecture) {
        if (lecture == null) {
            throw new IllegalArgumentException("Lecture cannot be null.");
        }
        if (lecture.date == null) {
            throw new IllegalArgumentException("Lecture date is required.");
        }
        if (isBlank(lecture.timeSlot)) {
            throw new IllegalArgumentException("Lecture time slot is required.");
        }
        if (isBlank(lecture.roomNumber)) {
            throw new IllegalArgumentException("Room number is required.");
        }
        if (isBlank(lecture.moduleName)) {
            throw new IllegalArgumentException("Module name is required.");
        }

        Set<String> moduleNames = new LinkedHashSet<>();
        for (Lecture existing : lectures) {
            moduleNames.add(existing.moduleName);
        }

        moduleNames.add(lecture.moduleName);

        if (moduleNames.size() > MAX_MODULES) {
            throw new IllegalArgumentException("Only up to five modules are supported for this course.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
