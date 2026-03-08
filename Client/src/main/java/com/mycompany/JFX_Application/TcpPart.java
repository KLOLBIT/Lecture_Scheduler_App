package com.mycompany.JFX_Application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpPart {
    private final String hostPart;
    private final int portN;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public TcpPart(String host, int port) {
        hostPart = host;
        portN = port;
    }

    public synchronized ServerPart sendToServer(String requestMsg) throws IOException {
        makeSureItIsConnected();
        writer.println(requestMsg);

        String rawReply = reader.readLine();
        if (rawReply == null) {
            throw new IOException("Server closed connection.");
        }

        return ServerPart.fromRawReply(rawReply);
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
}