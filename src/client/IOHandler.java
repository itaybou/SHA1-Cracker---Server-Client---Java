package client;

import protocol.MessageTypes;
import protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IOHandler {

    private BufferedReader reader;
    private MessageTypes type;
    private String hash;
    private byte originalLength;
    private String originalRangeStart;
    private String originalRangeEnd;
    private String teamName;

    private Protocol protocol;

    public IOHandler(String teamName, Protocol protocol) {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.protocol = protocol;
        this.teamName = teamName;
        this.type = MessageTypes.DISCOVER;
        this.originalRangeStart = "";
        this.originalRangeEnd = "";
    }

    public boolean getUserInput() {
        try {
            System.out.println("Welcome to " + teamName + ". Please enter the hash:");
            this.hash = reader.readLine();
            if (hash.length() > protocol.getMessageHashLength()) {
                throw new IllegalArgumentException();
            }
            System.out.println("Please enter the input string length:");
            this.originalLength = Byte.parseByte(reader.readLine());
            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }

    public void setRequest() {
        this.type = MessageTypes.REQUEST;
    }

    public void setRanges(String originalRangeStart, String originalRangeEnd) {
        this.originalRangeStart = originalRangeStart;
        this.originalRangeEnd = originalRangeEnd;
    }

    public byte getType() {
        return type.getType();
    }

    public String getHash() {
        return hash;
    }

    public byte getOriginalLength() {
        return originalLength;
    }

    public String getOriginalRangeStart() {
        return originalRangeStart;
    }

    public String getOriginalRangeEnd() {
        return originalRangeEnd;
    }

}
