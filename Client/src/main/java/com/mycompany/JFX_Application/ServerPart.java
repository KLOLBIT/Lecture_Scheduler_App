package com.mycompany.JFX_Application;

import java.io.IOException;

public class ServerPart {
    public final String rawResponse;
    public final int code;
    public final String body;

    public ServerPart(String raw, int responseCode, String responseBody) {
        rawResponse = raw;
        code = responseCode;
        body = responseBody;
    }

    public static ServerPart fromRawReply(String rawReply) throws IOException {
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

        return new ServerPart(rawReply, numberCode, bits[2]);
    }
}