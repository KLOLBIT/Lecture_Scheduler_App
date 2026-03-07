package services.scheduling;

import data.Lecture;
import data.WeekSchedule;
import data.exceptions.ScheduleConflictException;

import java.time.LocalDate;

public interface ISchedulingService {
    void addLecture(Lecture lecture) throws ScheduleConflictException;

    Lecture removeLecture(LocalDate date, String timeSlot);

    WeekSchedule getWeekSchedule();
}
