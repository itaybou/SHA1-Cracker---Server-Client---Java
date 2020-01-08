package client;

import protocol.Message;
import protocol.MessageTypes;
import protocol.Protocol;
import utils.HashCracker;

import java.util.Deque;
import java.util.LinkedList;

public class PacketHandler {

    private Protocol protocol;
    private UDPClient client;

    public PacketHandler(Protocol protocol, UDPClient client) {
        this.protocol = protocol;
        this.client = client;
    }

    public void parseMessage(Message message) {
        MessageTypes messageType = MessageTypes.valueOf(message.getData()[protocol.getMessageTypeIndex()]);
        switch(messageType) {
            case OFFER:
                System.out.println("Active server " + message.getAddress() + ":" + message.getPort() +" sent an offer.");
                client.addActiveServer(message);
                break;
            case ACK:
                printResult(message);
                client.removeAllServers();
                break;
            case NACK:
                System.out.println("Server at " +message.getAddress()+ ":" + message.getPort() +" returned NACK.");
                client.removeServer();
                break;
            default:
                break;
        }
    }

    private void printResult(Message message) {
        int dataIndex = protocol.getMessageStartRange();
        byte[] messageData = message.getData();
        StringBuilder sb = new StringBuilder();
        while(messageData[dataIndex] != protocol.NULL) {
            sb.append((char) messageData[dataIndex++]);
        }
        System.out.println("The input string is: " + sb.toString());
    }

    /**
     * Divides to @param partsCount equal ranges of character arrays starting from
     * 'a' * @param len to 'z' * @param len.
     * Ex. len := 3, partsCount := 3 => output = [{aaa, iri}, {irj, riq}, {rir, zzz}]
     * where the corresponding strings in range are [5859, 5858, 5858].
     *
     * @param len         Length of the characters arrays to divide
     * @param partsCount The amount of parts to divide arrays into.
     */
    public Deque<Range> divideEqualStringRanges(byte len, int partsCount) {
        final int ALPHABETIC_COUNT = 26;
        final long TOTAL_RANGE = (long) Math.pow(ALPHABETIC_COUNT, len);
        final long RANGE_COUNT = (int) Math.ceil(TOTAL_RANGE / (double) partsCount) - 1;
        if (len < 1 || partsCount < 1)
            return null;
        Deque<Range> output = new LinkedList<>();
        long currentRangeStart = 0, currentRangeEnd = RANGE_COUNT;
        for (int i = 0; i < partsCount; ++i) {
            output.add(new Range(HashCracker.decimalToAlphabetic(currentRangeStart), HashCracker.decimalToAlphabetic(currentRangeEnd), len));
            currentRangeStart = currentRangeEnd + 1;
            currentRangeEnd += i == partsCount - 2 ? TOTAL_RANGE - currentRangeEnd - 1 : RANGE_COUNT;
        }
        return output;
    }
}
