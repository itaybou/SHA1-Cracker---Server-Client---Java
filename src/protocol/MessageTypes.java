package protocol;

public enum MessageTypes {
    DISCOVER((byte) 1),
    OFFER((byte) 2),
    REQUEST((byte) 3),
    ACK((byte) 4),
    NACK((byte) 5);

    private byte type;

    MessageTypes(byte type) {
        this.type = type;
    }

    public static MessageTypes valueOf(byte type) {
        for(MessageTypes messageTypes : values()) {
            if(messageTypes.type == type) {
                return messageTypes;
            }
        }
        return NACK;
    }

    public byte getType() {
        return this.type;
    }
}
