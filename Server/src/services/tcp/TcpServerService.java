package services.tcp;

import controllers.ApiController;
import data.Lecture;
import data.exceptions.IncorrectActionException;
import data.exceptions.ScheduleConflictException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class TcpServerService {
    private final int port;
    private final ApiController controller;

    public TcpServerService(int port, ApiController controller) {
        this.port = port;
        this.controller = controller;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP server listening on 127.0.0.1:" + port);

            for (;;) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Client connected");
                    handleClient(clientSocket);
                    System.out.println("Client disconnected.");
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        var writer = new PrintWriter(clientSocket.getOutputStream(), true);

        String request;
        while ((request = reader.readLine()) != null) {
            writer.println(request.toUpperCase());

            var sections = request.split("\\|", -1);
            if (sections.length != 5) {
                var response = formatResponse(400, "Invalid request body");
                writer.println(response);
                continue;
            }

            var action = sections[0].trim().toUpperCase();
            if (action.equals("STOP")) {
                var response = formatResponse(200, "TERMINATE");
                writer.println(response);
                return;
            }

            var result = handleAction(
                    action,
                    normalizeValue(sections[1]),
                    normalizeValue(sections[2]),
                    normalizeValue(sections[3]),
                    normalizeValue(sections[4])
            );

            writer.println(result);
        }
    }

    private String handleAction(String action, String date, String time, String room, String module) {
        try {
            switch (action) {
                case "ADD" -> {
                    return formatResponse(200, controller.handleAdd(date, time, room, module));
                }
                case "REMOVE" -> {
                    var removedLecture = controller.handleRemove(date, time);
                    if (removedLecture == null) {
                        return formatResponse(404, "No lecture found at " + time + " on " + date + ".");
                    }

                    return formatResponse(200, "Lecture removed successfully from room " + removedLecture.roomNumber + " at " + removedLecture.timeSlot + " on " + removedLecture.date + ".");
                }
                case "DISPLAY" -> {
                    return formatResponse(200, serializeSchedule(controller.handleDisplay()));
                }
                default -> {
                    controller.handleOther(action);
                    return formatResponse(401, "");
                }
            }
        } catch (IncorrectActionException e) {
            return formatResponse(401, e.getMessage());
        } catch (ScheduleConflictException e) {
            return formatResponse(409, e.getMessage());
        } catch (IllegalArgumentException e) {
            return formatResponse(422, e.getMessage());
        }
    }

    private String normalizeValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String formatResponse(int code, String body) {
        return "RESULT|" + code + "|" + (body == null ? "" : body);
    }

    private String serializeSchedule(List<Lecture> lectures) {
        var builder = new StringBuilder();

        var firstLecture = lectures.stream().findFirst();
        if (firstLecture.isEmpty())
            return builder.toString();

        for (var lecture : lectures) {
            if (lecture != firstLecture.get())
                builder.append(';');

            builder.append(lecture.date)
                    .append(',')
                    .append(lecture.timeSlot)
                    .append(',')
                    .append(lecture.roomNumber)
                    .append(',')
                    .append(lecture.moduleName);
        }

        return builder.toString();
    }
}
