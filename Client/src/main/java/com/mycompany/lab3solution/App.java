package com.mycompany.lab3solution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import static javafx.application.Application.launch;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * SIMPLE version (for early stage students):
 * - No full MVC
 * - UI + events are in this class
 * - "Server" is simulated by calling server.handle(requestString)
 *
 * Request format:
 *   ACTION|DATE|TIME|ROOM|MODULE
 */
public class App extends Application {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 1234;

    private ComboBox<String> actionBox;
    private DatePicker datePicker;
    private ComboBox<String> timeBox;
    private TextField roomField;
    private TextField moduleField;

    private Button sendBtn;
    private Button stopBtn;
    private Button clearBtn;

    private TextArea logArea;
    private Label statusLabel;

    private TableView<Row> table;

    private final TcpClient client = new TcpClient(SERVER_HOST, SERVER_PORT);
    private boolean stopped = false;
    private boolean requestInProgress = false;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        root.setTop(buildHeader());
        root.setLeft(buildForm());
        root.setCenter(buildTable());
        root.setBottom(buildLog());

        Scene scene = new Scene(root, 980, 650);
        stage.setTitle("Lecture Scheduler Client (Simple Lab)");
        stage.setScene(scene);
        stage.show();

        refreshTableFromServer();
    }

    @Override
    public void stop() {
        client.closeQuietly();
    }

    private Node buildHeader() {
        Label title = new Label("Lecture Scheduler Client (Simple Lab – Network-Style Messages)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        HBox header = new HBox(title);
        header.setPadding(new Insets(12));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #f2f2f2;");
        return header;
    }

    private Node buildForm() {
        actionBox = new ComboBox<>(FXCollections.observableArrayList("ADD", "REMOVE", "DISPLAY", "OTHER"));
        actionBox.getSelectionModel().selectFirst();

        datePicker = new DatePicker();
        timeBox = new ComboBox<>(FXCollections.observableArrayList(
                "09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00",
                "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"
        ));
        timeBox.getSelectionModel().selectFirst();

        roomField = new TextField();
        roomField.setPromptText("e.g., C105");

        moduleField = new TextField();
        moduleField.setPromptText("e.g., CS6502");

        sendBtn = new Button("Send Request");
        stopBtn = new Button("STOP");
        clearBtn = new Button("Clear");

        statusLabel = new Label("Status: Ready");

        sendBtn.setMaxWidth(Double.MAX_VALUE);
        stopBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setMaxWidth(Double.MAX_VALUE);

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setHgap(10);
        form.setVgap(10);

        int r = 0;
        form.add(new Label("Action:"), 0, r);
        form.add(actionBox, 1, r++);

        form.add(new Label("Date:"), 0, r);
        form.add(datePicker, 1, r++);

        form.add(new Label("Time slot:"), 0, r);
        form.add(timeBox, 1, r++);

        form.add(new Label("Room:"), 0, r);
        form.add(roomField, 1, r++);

        form.add(new Label("Module:"), 0, r);
        form.add(moduleField, 1, r++);

        VBox buttons = new VBox(8, sendBtn, stopBtn, clearBtn, statusLabel);
        buttons.setPadding(new Insets(12, 0, 0, 0));

        VBox left = new VBox(8, new Label("Request Builder"), form, buttons);
        left.setPadding(new Insets(12));
        left.setPrefWidth(360);
        left.setStyle("-fx-border-color: #dddddd; -fx-border-width: 0 1 0 0;");

        // --- events ---
        sendBtn.setOnAction(e -> onSend());
        stopBtn.setOnAction(e -> onStop());
        clearBtn.setOnAction(e -> onClear());

        // small usability: disable some fields for DISPLAY
        actionBox.valueProperty().addListener((obs, oldV, newV) -> {
            boolean needsDateTime = "ADD".equals(newV) || "REMOVE".equals(newV);
            datePicker.setDisable(!needsDateTime);
            timeBox.setDisable(!needsDateTime);
            roomField.setDisable(!"ADD".equals(newV));
            moduleField.setDisable(!"ADD".equals(newV));
        });

        return left;
    }

    private Node buildTable() {
        table = new TableView<>();

        TableColumn<Row, String> cDate = new TableColumn<>("Date");
        cDate.setCellValueFactory(d -> d.getValue().date);

        TableColumn<Row, String> cTime = new TableColumn<>("Time");
        cTime.setCellValueFactory(d -> d.getValue().time);

        TableColumn<Row, String> cRoom = new TableColumn<>("Room");
        cRoom.setCellValueFactory(d -> d.getValue().room);

        TableColumn<Row, String> cModule = new TableColumn<>("Module");
        cModule.setCellValueFactory(d -> d.getValue().module);

        table.getColumns().addAll(cDate, cTime, cRoom, cModule);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox center = new VBox(8, new Label("Schedule (TableView)"), table);
        center.setPadding(new Insets(12));
        return center;
    }

    private Node buildLog() {
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(6);

        VBox bottom = new VBox(6, new Label("Conversation Log (Client ↔ Server)"), logArea);
        bottom.setPadding(new Insets(12));
        bottom.setStyle("-fx-background-color: #fafafa; -fx-border-color: #dddddd; -fx-border-width: 1 0 0 0;");
        return bottom;
    }

    private void onSend() {
        if (stopped) {
            alertInfo("The connection is stopped. Press Clear to start again.");
            return;
        }

        if (requestInProgress) {
            return;
        }

        String action = actionBox.getValue();
        String request = buildRequest(action, datePicker.getValue(), timeBox.getValue(), roomField.getText(), moduleField.getText());
        if (request == null) {
            return;
        }

        boolean explicitDisplay = "DISPLAY".equals(action);
        boolean refreshAfter = !explicitDisplay;
        executeRequest(request, explicitDisplay, refreshAfter, false);
    }

    private void onStop() {
        if (stopped || requestInProgress) {
            return;
        }

        executeRequest("STOP||||", false, false, true);
    }

    private void onClear() {
        roomField.clear();
        moduleField.clear();
        datePicker.setValue(null);
        actionBox.getSelectionModel().selectFirst();
        timeBox.getSelectionModel().selectFirst();

        stopped = false;
        requestInProgress = false;
        statusLabel.setText("Status: Ready");
        updateButtonStates();

        log("--- cleared ---");
        refreshTableFromServer();
    }

    private String buildRequest(String action, LocalDate date, String time, String room, String module) {
        // Keep the exact required format: ACTION|DATE|TIME|ROOM|MODULE
        String d = (date == null) ? "" : date.toString();
        String t = (time == null) ? "" : time.trim();
        String r = (room == null) ? "" : room.trim();
        String m = (module == null) ? "" : module.trim();

        // For DISPLAY/OTHER, we can keep fields empty to keep it simple.
        if ("DISPLAY".equals(action)) return "DISPLAY||||";
        if ("OTHER".equals(action)) return "OTHER||||";

        if ("ADD".equals(action)) {
            // basic validation (simple)
            if (d.isEmpty() || t.isEmpty() || r.isEmpty() || m.isEmpty()) {
                alertWarn("ADD needs Date, Time, Room and Module.");
                return "DISPLAY||||"; // safe fallback
            }
            return "ADD|" + d + "|" + t + "|" + r + "|" + m;
        }

        if ("REMOVE".equals(action)) {
            if (d.isEmpty() || t.isEmpty()) {
                alertWarn("REMOVE needs Date and Time.");
                return "DISPLAY||||";
            }
            return "REMOVE|" + d + "|" + t + "||";
        }

        return "OTHER||||";
    }

    private void refreshTableFromServer() {
        if (requestInProgress || stopped) {
            return;
        }

        executeRequest("DISPLAY||||", true, false, false);
    }

    private void executeRequest(String request, boolean updateTableFromResponse, boolean refreshAfter, boolean stopRequest) {
        requestInProgress = true;
        updateButtonStates();

        Thread worker = new Thread(() -> {
            try {
                String responseText = client.send(request);
                ServerResponse response = parseResponse(responseText);

                ServerResponse displayResponse = null;
                if (refreshAfter && !stopRequest) {
                    displayResponse = parseResponse(client.send("DISPLAY||||"));
                }

                if (stopRequest) {
                    client.closeQuietly();
                }

                ServerResponse finalDisplayResponse = displayResponse;
                Platform.runLater(() -> applyServerResponses(request, response, finalDisplayResponse, updateTableFromResponse, stopRequest));
            } catch (IOException | IllegalArgumentException e) {
                client.closeQuietly();
                Platform.runLater(() -> {
                    requestInProgress = false;
                    updateButtonStates();
                    statusLabel.setText("Status: Connection Error");
                    log("CLIENT> " + request);
                    log("SERVER> ERROR: " + e.getMessage());
                    alertWarn("Could not communicate with the server. " + e.getMessage());
                });
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    private void applyServerResponses(String request, ServerResponse response, ServerResponse displayResponse, boolean updateTableFromResponse, boolean stopRequest) {
        requestInProgress = false;
        log("CLIENT> " + request);
        log("SERVER> " + response.raw);

        if (stopRequest && response.code == 200 && "TERMINATE".equals(response.body)) {
            stopped = true;
            statusLabel.setText("Status: TERMINATED");
            sendBtn.setDisable(true);
            stopBtn.setDisable(true);
            return;
        }

        statusLabel.setText(response.code == 200 ? "Status: OK (200)" : "Status: ERROR (" + response.code + ")");

        if (updateTableFromResponse && response.code == 200) {
            updateTable(response.body);
        } else if (displayResponse != null && displayResponse.code == 200) {
            updateTable(displayResponse.body);
        }

        updateButtonStates();
    }

    private ServerResponse parseResponse(String responseText) throws IOException {
        if (responseText == null) {
            throw new IOException("Server closed the connection.");
        }

        String[] parts = responseText.split("\\|", 3);
        if (parts.length != 3 || !"RESULT".equals(parts[0])) {
            throw new IOException("Invalid response: " + responseText);
        }

        int code;
        try {
            code = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid response code: " + responseText);
        }

        return new ServerResponse(responseText, code, parts[2]);
    }

    private void updateTable(String payload) {
        List<Row> rows = new ArrayList<>();

        if (payload != null && !payload.isBlank()) {
            String[] entries = payload.split(";");
            for (String entry : entries) {
                if (entry.isBlank()) {
                    continue;
                }

                String[] fields = entry.split(",", 4);
                if (fields.length == 4) {
                    rows.add(new Row(fields[0], fields[1], fields[2], fields[3]));
                }
            }
        }
        table.setItems(FXCollections.observableArrayList(rows));
    }

    private void updateButtonStates() {
        sendBtn.setDisable(stopped || requestInProgress);
        stopBtn.setDisable(stopped || requestInProgress);
    }

    private void log(String msg) {
        logArea.appendText(msg + System.lineSeparator());
    }

    private void alertWarn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText("Validation");
        a.showAndWait();
    }

    private void alertInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText("Info");
        a.showAndWait();
    }

    /** TableView row */
    public static class Row {
        final SimpleStringProperty date;
        final SimpleStringProperty time;
        final SimpleStringProperty room;
        final SimpleStringProperty module;

        Row(String date, String time, String room, String module) {
            this.date = new SimpleStringProperty(date);
            this.time = new SimpleStringProperty(time);
            this.room = new SimpleStringProperty(room);
            this.module = new SimpleStringProperty(module);
        }
    }

    private static class ServerResponse {
        final String raw;
        final int code;
        final String body;

        ServerResponse(String raw, int code, String body) {
            this.raw = raw;
            this.code = code;
            this.body = body;
        }
    }

    private static class TcpClient {
        private final String host;
        private final int port;

        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;

        TcpClient(String host, int port) {
            this.host = host;
            this.port = port;
        }

        synchronized String send(String request) throws IOException {
            ensureConnected();
            writer.println(request);
            return reader.readLine();
        }

        synchronized void closeQuietly() {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
            }

            if (writer != null) {
                writer.close();
            }

            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ignored) {
            }

            reader = null;
            writer = null;
            socket = null;
        }

        private void ensureConnected() throws IOException {
            if (socket != null && socket.isConnected() && !socket.isClosed()) {
                return;
            }

            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
