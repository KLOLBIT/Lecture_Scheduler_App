package data;

import java.util.ArrayList;

public class DaySchedule {
    public ArrayList<Lecture> lectures;

    public DaySchedule() {
        this.lectures = new ArrayList<Lecture>();
    }

    public DaySchedule(ArrayList<Lecture> lectures) {
        this.lectures = lectures;
    }
}
