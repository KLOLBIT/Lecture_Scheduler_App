package controllers;

import data.Lecture;
import data.exceptions.IncorrectActionException;
import data.exceptions.ScheduleConflictException;
import services.scheduling.SchedulingService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ApiController {
    private final SchedulingService schedulingService;

    public ApiController() {
        schedulingService = new SchedulingService();
    }

    public String handleAdd(String dateText, String timeSlot, String roomNumber, String moduleName) throws ScheduleConflictException {
        var lecture = new Lecture(parseDate(dateText), timeSlot, roomNumber, moduleName);
        schedulingService.addLecture(lecture);
        return "Lecture added";
    }

    public Lecture handleRemove(String dateText, String timeSlot) {
        return schedulingService.removeLecture(parseDate(dateText), timeSlot);
    }

    public List<Lecture> handleDisplay() {
        return schedulingService.getSchedule();
    }

    public void handleOther(String action) throws IncorrectActionException {
        throw new IncorrectActionException("Unsupported action: " + action);
    }

    private LocalDate parseDate(String dateText) {
        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date must be in yyyy-MM-dd format.");
        }
    }
}
