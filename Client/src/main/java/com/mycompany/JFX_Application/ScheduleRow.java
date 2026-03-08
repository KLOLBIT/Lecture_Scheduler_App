package com.mycompany.JFX_Application;

import javafx.beans.property.SimpleStringProperty;

public class ScheduleRow {
    private final SimpleStringProperty dateCol = new SimpleStringProperty();
    private final SimpleStringProperty timeCol = new SimpleStringProperty();
    private final SimpleStringProperty roomCol = new SimpleStringProperty();
    private final SimpleStringProperty moduleCol = new SimpleStringProperty();

    public ScheduleRow(String d, String t, String r, String m) {
        dateCol.set(d);
        timeCol.set(t);
        roomCol.set(r);
        moduleCol.set(m);
    }

    public String getDate() {
        return dateCol.get();
    }

    public String getTime() {
        return timeCol.get();
    }

    public String getRoom() {
        return roomCol.get();
    }

    public String getModule() {
        return moduleCol.get();
    }
}