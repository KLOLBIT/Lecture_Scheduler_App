package com.mycompany.JFX_Application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpService {
    private final String hostPart;
    private final int portN;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public TcpService(String host, int port) {
        hostPart = host;
        portN = port;
    }

    public synchronized Response sendToServer(String requestMsg) throws IOException {
        makeSureItIsConnected();
        writer.println(requestMsg);

        String rawReply;
        while ((rawReply = reader.readLine()) != null) {
            if (rawReply.startsWith("RESULT|")) {
                return Response.fromRawReply(rawReply);
            }
        }

        throw new IOException("Server closed connection.");
    }

    private void makeSureItIsConnected() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }

        socket = new Socket(hostPart, portN);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public synchronized void closeQuietly() {
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

    public static class Response {
        public final String rawResponse;
        public final int code;
        public final String body;

        public Response(String raw, int responseCode, String responseBody) {
            rawResponse = raw;
            code = responseCode;
            body = responseBody;
        }

        public static Response fromRawReply(String rawReply) throws IOException {
            String[] bits = rawReply.split("\\|", 3);

            if (bits.length != 3) {
                throw new IOException("Inappropriate server reply: " + rawReply);
            }

            if (!bits[0].equals("RESULT")) {
                throw new IOException("Inappropriate server reply: " + rawReply);
            }

            int numberCode;
            try {
                numberCode = Integer.parseInt(bits[1]);
            } catch (NumberFormatException e) {
                throw new IOException("Inappropriate server code; " + rawReply);
            }

            return new Response(rawReply, numberCode, bits[2]);
        }
    }
}
