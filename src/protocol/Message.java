package protocol;

import java.net.InetAddress;

public class Message {
    private InetAddress address;
    private int port;
    private byte[] data;
    private boolean ignore;

    public Message(InetAddress address, int port, byte[] data) {
        this.address = address;
        this.port = port;
        this.data = data;
        this.ignore = false;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public byte[] getData() {
        return data;
    }

    public void setIgnore() {
        this.ignore = true;
    }

    public boolean isIgnore() {
        return ignore;
    }
}
