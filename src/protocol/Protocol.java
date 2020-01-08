package protocol;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Protocol {

    private final Charset ENCODING = StandardCharsets.UTF_8;

    private final String TEAM_NAME = "Cybers";
    private final int INIT_BUFFER_SIZE = 74, BUFFER_SIZE = 586;
    private final int MSG_NAME_LEN = 32, MSG_HASH_LEN = 40, MSG_RANGE_LEN = 256;
    private final int MSG_NAME = 0, MSG_TYPE = 32, MSG_HASH = 33, MSG_ORIG_LEN = 73, MSG_RANGE_START = 74;
    public final char NULL = '\0';

    public byte[] createMessage(byte type, String hash, String rangeStart, String rangeEnd, byte originalLength) {
        return ByteBuffer.allocate(INIT_BUFFER_SIZE + 2 * originalLength)
                .put(padStringRight(TEAM_NAME, MSG_NAME_LEN)).put(type).put(padStringRight(hash, MSG_HASH_LEN))
                .put(originalLength).put(rangeStart.getBytes(ENCODING)).put(rangeEnd.getBytes(ENCODING))
                .array();
    }

    private byte[] padStringRight(String str, int pad) {
        return String.format("%-" + pad + "s", str).replace(' ', NULL).getBytes(ENCODING);
    }

    public int getMessageSize() {
        return BUFFER_SIZE;
    }

    public int getMessageHashLength() {
        return MSG_HASH_LEN;
    }

    public int getMessageHashIndex() {
        return MSG_HASH;
    }

    public int getMessageStartRange() {
        return MSG_RANGE_START;
    }

    public int getMessageRangesLength() {
        return MSG_RANGE_LEN;
    }

    public int getMessageOriginLengthIndex() {
        return MSG_ORIG_LEN;
    }

    public int getMessageTypeIndex() {
        return MSG_TYPE;
    }

    public String getTeamName() {
        return TEAM_NAME;
    }

}
