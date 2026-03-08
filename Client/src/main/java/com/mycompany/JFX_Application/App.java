package com.mycompany.JFX_Application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
    private final TcpService tcpService = new TcpService("127.0.0.1", 1234);

    private ComboBox<String> actionSlot;
    private DatePicker calendarSlot;
    private ComboBox<String> timeSlot;
    private TextField roomSlot;
    private TextField moduleSlot;
    private CheckBox autoRefreshCheckBox;

    private Button Send;
    private Button Stop;
    private Button Clear;

    private Label statusLabel;
    private TextArea textInput;

    private TableView<ScheduleRow> scheduleView;
    private final ObservableList<ScheduleRow> tableList = FXCollections.observableArrayList();

    private boolean terminated_status = false;

    @Override
    public void start(Stage stage) {

        statusLabel = new Label("Status: Connected to the server");
        statusLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: #18d037;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 10 10 10 10;" +
                "-fx-font-family: monospace;" +
                "-fx-text-fill: black;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #00fffb;"
        );

        actionSlot = new ComboBox<>();
        actionSlot.getItems().addAll("ADD", "REMOVE", "DISPLAY", "OTHER");
        actionSlot.getSelectionModel().selectFirst();
        actionSlot.setStyle("-fx-font-size: 14px;" + "-fx-font-family: monospace;");

        calendarSlot = new DatePicker();
        calendarSlot.setStyle("-fx-font-size: 14px;" + "-fx-font-family: monospace;");

        timeSlot = new ComboBox<>();
        timeSlot.getItems().addAll(
                "09:00-10:00",
                "10:00-11:00",
                "11:00-12:00",
                "12:00-13:00",
                "14:00-15:00",
                "15:00-16:00"
        );
        timeSlot.setStyle("-fx-font-size: 14px;" + "-fx-font-family: monospace;");

        roomSlot = new TextField();
        roomSlot.setPromptText("Enter room");
        roomSlot.setStyle("-fx-font-size: 14px; -fx-background-radius: 6;" + "-fx-font-family: monospace;");

        moduleSlot = new TextField();
        moduleSlot.setPromptText("Enter module");
        moduleSlot.setStyle("-fx-font-size: 14px; -fx-background-radius: 6;" + "-fx-font-family: monospace;");

        autoRefreshCheckBox = new CheckBox("Auto Refresh");
        autoRefreshCheckBox.setSelected(false);
        autoRefreshCheckBox.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: monospace; -fx-text-fill: #18d037;");

        Send = new Button("Send Request");
        Stop = new Button("Stop");
        Clear = new Button("Clear");

        Send.setStyle(
                "-fx-background-color: #18d037;" +
                "-fx-text-fill: green;" +
                "-fx-font-size: 14px;" +
                "-fx-border-color: #00fffb;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;"
        );

        Stop.setStyle(
                "-fx-background-color: #18d037;" +
                "-fx-text-fill: red;" +
                "-fx-font-size: 14px;" +
                "-fx-border-color: #00fffb;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;"
        );

        Clear.setStyle(
                "-fx-background-color: #18d037;" +
                "-fx-text-fill: purple;" +
                "-fx-font-size: 14px;" +
                "-fx-border-color: #00fffb;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;"
        );

        HBox rowButton = new HBox(10, Send, Stop, Clear);
        rowButton.setAlignment(Pos.CENTER_RIGHT);
        rowButton.setMaxWidth(Double.MAX_VALUE);

        FlowPane controlRow = new FlowPane();
        controlRow.setHgap(16);
        controlRow.setVgap(12);
        controlRow.setAlignment(Pos.CENTER_LEFT);
        controlRow.getChildren().addAll(
                addControlField("Action", actionSlot),
                addControlField("Date", calendarSlot),
                addControlField("Time", timeSlot),
                addControlField("Room", roomSlot),
                addControlField("Module", moduleSlot),
                addControlField("Options", autoRefreshCheckBox)
        );

        textInput = new TextArea();
        textInput.setEditable(false);
        textInput.setStyle(
                "-fx-font-family: monospace;" +
                "-fx-font-size: 13px;" +
                "-fx-control-inner-background: #2b2b2b;" +
                "-fx-background-radius: 10;" +
                "-fx-text-fill: #18d037;" +
                "-fx-border-color: #00fffb;" + 
                "-fx-border-radius: 14;"
        );

        scheduleView = new TableView<>();
        scheduleView.setItems(tableList);
        scheduleView.setStyle(
        "-fx-control-inner-background: #d9ffd9;" +
        "-fx-background-color: #d9ffd9;" +
        "-fx-font-size: 13px;"
        );
        
        TableColumn<ScheduleRow, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<ScheduleRow, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        TableColumn<ScheduleRow, String> colRoom = new TableColumn<>("Room");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("room"));

        TableColumn<ScheduleRow, String> colModule = new TableColumn<>("Module");
        colModule.setCellValueFactory(new PropertyValueFactory<>("module"));

        scheduleView.getColumns().addAll(colDate, colTime, colRoom, colModule);

        VBox centerPart = new VBox(12, buildSectionTitle("Timetable"), scheduleView);
        centerPart.setPadding(new Insets(16));
        centerPart.setStyle(
                "-fx-background-color: #2b2b2b;" +
                "-fx-border-color: #00fffb;" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;"
        );
        VBox.setVgrow(scheduleView, Priority.ALWAYS);

        VBox controlsCard = new VBox(12, buildSectionTitle("Controls"), controlRow, rowButton);
        controlsCard.setPadding(new Insets(16));
        controlsCard.setStyle(
                "-fx-background-color: #2b2b2b;" +
                "-fx-border-color: #00fffb;" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;"
        );

        VBox workspace = new VBox(12, centerPart, controlsCard);
        VBox.setVgrow(centerPart, Priority.ALWAYS);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        VBox consoleCard = new VBox(12, buildSectionTitle("Console"), textInput);
        consoleCard.setPadding(new Insets(16));
        consoleCard.setStyle(
                "-fx-background-color: #2b2b2b;" +
                "-fx-border-color: #00fffb;" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;"
        );
        VBox.setVgrow(textInput, Priority.ALWAYS);

        VBox statusCard = new VBox(12, buildSectionTitle("Connection"), statusLabel);
        statusCard.setPadding(new Insets(16));
        statusCard.setStyle(
                "-fx-background-color: #2b2b2b;" +
                "-fx-border-color: #00fffb;" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;"
        );

        VBox rightSidebar = new VBox(12, statusCard, consoleCard);
        rightSidebar.setPrefWidth(380);
        VBox.setVgrow(consoleCard, Priority.ALWAYS);

        HBox mainContent = new HBox(12, workspace, rightSidebar);

        BorderPane mainArea = new BorderPane();
        mainArea.setTop(buildHeader());
        mainArea.setCenter(mainContent);
        BorderPane.setMargin(mainContent, new Insets(12, 0, 0, 0));
        mainArea.setPadding(new Insets(12));
        mainArea.setStyle("-fx-background-color: #2b2b2b;");

        Send.setOnAction(e -> serverRequest());

        Stop.setOnAction(e -> serverStop());

        Clear.setOnAction(e -> {
            calendarSlot.setValue(null);
            timeSlot.setValue(null);
            roomSlot.clear();
            moduleSlot.clear();

            terminated_status = false;
            Send.setDisable(false);
            Stop.setDisable(false);
            statusLabel.setText("Status: Connected to the server");
        });

        Scene scene = new Scene(mainArea, 1380, 860);

        stage.setTitle("Lecture Scheduler Application");
        stage.setScene(scene);
        stage.show();

        refillTableFromServer();
    }

    private VBox buildHeader() {
        Label title = new Label("Lecture Scheduler");
        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: black;" + 
                "-fx-font-family: monospace;"
        );
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Application for managing the lecture timetable. Prioritize, Manage, Deliver");
        subtitle.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #2b2b2b;"
        );

        VBox headerBox = new VBox(4, title, subtitle);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(18));
        headerBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #18d037, #2b2b2b);" +
                "-fx-background-radius: 14;"
        );

        return headerBox;
    }

    private Label buildSectionTitle(String text) {
        Label sectionTitle = new Label(text);
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: monospace; -fx-text-fill: #18d037;");
        return sectionTitle;
    }

    private VBox addControlField(String labelText, Control inputControl) {
        Label controlLabel = new Label(labelText + ":");
        controlLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: monospace; -fx-text-fill: #18d037;");

        VBox fieldBox = new VBox(8, controlLabel, inputControl);
        fieldBox.setAlignment(Pos.TOP_LEFT);
        return fieldBox;
    }

    private void serverRequest() {

        if (terminated_status) {
            textInput.appendText("CLIENT -> Connection was stopped. Press Clear to use it again.\n");
            return;
        }

        String action = actionSlot.getValue();

        String date = "";
        String time = "";
        String room = "";
        String module = "";

        if (calendarSlot.getValue() != null)
            date = calendarSlot.getValue().toString();

        if (timeSlot.getValue() != null)
            time = timeSlot.getValue();

        room = roomSlot.getText();
        module = moduleSlot.getText();

        if (action.equals("DISPLAY")) {
            date = "";
            time = "";
            room = "";
            module = "";
        }

        if (action.equals("REMOVE")) {
            room = "";
            module = "";
        }

        if (action.equals("OTHER")) {
            date = "";
            time = "";
            room = "";
            module = "";
        }

        String requestMsg = action + "|" + date + "|" + time + "|" + room + "|" + module;

        textInput.appendText("CLIENT -> " + requestMsg + "\n");

        try {
            TcpService.Response serverReply = tcpService.sendToServer(requestMsg);

            textInput.appendText("SERVER -> " + serverReply.rawResponse + "\n");
            statusLabel.setText("Status: " + serverReply.code);

            if (action.equals("DISPLAY") && serverReply.code == 200) {
                refillTableFromPayload(serverReply.body);
            }

            if ((action.equals("ADD") || action.equals("REMOVE"))
                    && serverReply.code == 200
                    && autoRefreshCheckBox.isSelected()) {
                refillTableFromServer();
            }

        } catch (Exception ex) {
            textInput.appendText("SERVER -> ERROR|" + ex.getMessage() + "\n");
            statusLabel.setText("Status: Connection Error");
        }
    }

    private void serverStop() {

        if (terminated_status) {
            return;
        }

        String requestMsg = "STOP||||";

        textInput.appendText("CLIENT -> " + requestMsg + "\n");

        try {
            TcpService.Response serverReply = tcpService.sendToServer(requestMsg);

            textInput.appendText("SERVER -> " + serverReply.rawResponse + "\n");

            if (serverReply.code == 200 && serverReply.body.equals("TERMINATE")) {
                terminated_status = true;
                Send.setDisable(true);
                Stop.setDisable(true);
                statusLabel.setText("Status: TERMINATED");
                tcpService.closeQuietly();
            } else {
                statusLabel.setText("Status: " + serverReply.code);
            }

        } catch (Exception ex) {
            textInput.appendText("SERVER -> ERROR|" + ex.getMessage() + "\n");
            statusLabel.setText("Status: Connection Error");
        }
    }

    private void refillTableFromServer() {

        if (terminated_status) {
            return;
        }

        try {
            TcpService.Response serverReply = tcpService.sendToServer("DISPLAY||||");

            textInput.appendText("CLIENT -> DISPLAY||||\n");
            textInput.appendText("SERVER -> " + serverReply.rawResponse + "\n");

            if (serverReply.code == 200) {
                refillTableFromPayload(serverReply.body);
            }

        } catch (Exception ex) {
            textInput.appendText("SERVER -> ERROR|" + ex.getMessage() + "\n");
            statusLabel.setText("Status: Connection Error");
        }
    }

    private void refillTableFromPayload(String payload) {

        tableList.clear();

        if (payload == null || payload.isBlank()) {
            return;
        }

        String[] lectureBits = payload.split(";");

        for (String oneLecture : lectureBits) {
            String[] lectureParts = oneLecture.split(",", 4);

            if (lectureParts.length == 4) {
                tableList.add(
                        new ScheduleRow(
                                lectureParts[0],
                                lectureParts[1],
                                lectureParts[2],
                                lectureParts[3]
                        )
                );
            }
        }
    }

    @Override
    public void stop() {
        tcpService.closeQuietly();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
