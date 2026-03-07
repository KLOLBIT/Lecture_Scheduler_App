package data;

import java.time.DayOfWeek;
import java.util.HashMap;

public class WeekSchedule {
    public HashMap<DayOfWeek, DaySchedule> timeTableObject;

    public WeekSchedule() {
        this.timeTableObject = new HashMap<DayOfWeek, DaySchedule>();
        this.timeTableObject.put(DayOfWeek.MONDAY, new DaySchedule());
        this.timeTableObject.put(DayOfWeek.TUESDAY, new DaySchedule());
        this.timeTableObject.put(DayOfWeek.WEDNESDAY, new DaySchedule());
        this.timeTableObject.put(DayOfWeek.THURSDAY, new DaySchedule());
        this.timeTableObject.put(DayOfWeek.FRIDAY, new DaySchedule());
    }

    public WeekSchedule(HashMap<DayOfWeek, DaySchedule> timeTableObject) {
        if (timeTableObject == null) {
            throw new IllegalArgumentException("timeTableObject cannot be null");
        }
        this.timeTableObject = timeTableObject;
    }

    public DaySchedule getDaySchedule(DayOfWeek day) {
        return timeTableObject.get(day);
    }

    public void addLecture(Lecture lecture) {
        if (lecture == null || lecture.getDayOfWeek() == null) {
            return;
        }

        DayOfWeek dayOfWeek = lecture.getDayOfWeek();
        timeTableObject.putIfAbsent(dayOfWeek, new DaySchedule());
        timeTableObject.get(dayOfWeek).lectures.add(lecture);
    }
}
