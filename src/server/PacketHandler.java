package server;

import protocol.Protocol;
import utils.HashCracker;
import protocol.MessageTypes;
import protocol.Message;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;

public class PacketHandler implements Runnable {

    private UDPServer server;
    private Protocol protocol;
    private byte[] packetData;
    private InetAddress address;
    private int port;

    private static final String EMPTY = "";

    public PacketHandler(DatagramPacket packet, UDPServer server) {
        this.packetData = packet.getData();
        this.address = packet.getAddress();
        this.port = packet.getPort();
        this.server = server;
        this.protocol = server.getProtocol();
    }

    private void identifyMessage(){
        byte messageType = getMessageType();
        switch (MessageTypes.valueOf(messageType)) {
            case DISCOVER:
                server.addMessage(new Message(address, port, createOfferMessage()));
                break;
            case REQUEST:
                server.addMessage(new Message(address, port, createAckNackMessage()));
                break;
            default:
                break;
        }
    }

    private byte[] createOfferMessage() {
        return protocol.createMessage(MessageTypes.OFFER.getType(), EMPTY, EMPTY, EMPTY, packetData[protocol.getMessageOriginLengthIndex()]);
    }

    private byte[] createAckNackMessage() {
        long curr = System.currentTimeMillis();
        byte strLength = packetData[protocol.getMessageOriginLengthIndex()];
        try {
            String result = HashCracker.searchInRange(getMessageRange(true), getMessageRange(false), getMessageHash(), strLength);
            System.out.println("Time elapsed: " + ((System.currentTimeMillis() - curr) / 1000.0));
            return result == null ? protocol.createMessage(MessageTypes.NACK.getType(), EMPTY, EMPTY, EMPTY, strLength) :
                    protocol.createMessage(MessageTypes.ACK.getType(), EMPTY, result, EMPTY, strLength);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return protocol.createMessage(MessageTypes.NACK.getType(), EMPTY, EMPTY, EMPTY, strLength);
        }
    }

    private String getRangeFromMessageAsString(int from, int to) {
        StringBuilder sb = new StringBuilder();
        for(int i = from; i < to && packetData[i] != protocol.NULL; ++i) {
            sb.append((char) this.packetData[i]);
        }
        return sb.toString();
    }

    private byte getMessageType() {
        return this.packetData[protocol.getMessageTypeIndex()];
    }

    private String getMessageHash() {
        int hashIndex = protocol.getMessageHashIndex();
        return getRangeFromMessageAsString(hashIndex, hashIndex + protocol.getMessageHashLength());
    }

    private String getMessageRange(boolean start) {
        byte strLength = packetData[protocol.getMessageOriginLengthIndex()];
        int startRangeIndex = protocol.getMessageStartRange();
        if(start) {
            return getRangeFromMessageAsString(startRangeIndex, startRangeIndex + strLength);
        } else return getRangeFromMessageAsString(startRangeIndex + strLength, startRangeIndex + strLength * 2);
    }

    @Override
    public void run() {
        identifyMessage();
    }
}
