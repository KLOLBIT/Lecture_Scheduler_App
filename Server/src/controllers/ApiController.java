package controllers;

import data.Lecture;
import data.WeekSchedule;
import data.exceptions.IncorrectActionException;
import data.exceptions.ScheduleConflictException;
import services.scheduling.ISchedulingService;
import services.scheduling.SchedulingService;

import java.time.LocalDate;

public class ApiController {
    private final ISchedulingService schedulingService;

    public ApiController() {
        this(new SchedulingService());
    }

    public ApiController(ISchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    public String addLecture(LocalDate date, String timeSlot, String roomNumber, String moduleName) {
        Lecture lecture = new Lecture(date, timeSlot, roomNumber, moduleName);

        try {
            schedulingService.addLecture(lecture);
        } catch (ScheduleConflictException | IllegalArgumentException e) {
            return e.getMessage();
        }

        return "Lecture scheduled successfully for " + moduleName + " in room " + roomNumber + " at " + timeSlot + " on " + date + ".";
    }

    public String removeLecture(LocalDate date, String timeSlot) {
        Lecture removedLecture = schedulingService.removeLecture(date, timeSlot);
        if (removedLecture == null) {
            return "No lecture found at " + timeSlot + " on " + date + ".";
        }

        return "Removed lecture and freed room " + removedLecture.roomNumber + " at " + removedLecture.timeSlot + " on " + removedLecture.date + ".";
    }

    public WeekSchedule displaySchedule() {
        return schedulingService.getWeekSchedule();
    }

    public String handleOther(String action) throws IncorrectActionException {
        throw new IncorrectActionException("Unsupported action: " + action);
    }

    public String stop() {
        return "TERMINATE";
    }
}
