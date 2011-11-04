package cnt5106c.torrent.messages;
import java.io.IOException;

public class BitfieldMessage extends PayloadMessage
{
    private static final long serialVersionUID = 12L;

    public BitfieldMessage(byte[] bitField) throws IOException, InterruptedException
    {
        super(MessageType.bitfield, bitField);
    }
};
