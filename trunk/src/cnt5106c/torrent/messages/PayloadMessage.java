package cnt5106c.torrent.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cnt5106c.torrent.utils.Utilities;

public class PayloadMessage extends ActualMessage
{
    private static final long serialVersionUID = 4L;

    public PayloadMessage(MessageType msgType, byte[] payload) throws IOException, InterruptedException
    {
        super(msgType);
        ByteArrayOutputStream baos = Utilities.getStreamHandle();
        baos.write(super.message);
        baos.write(payload);
        super.message = baos.toByteArray();
    }
    
    /**
     * This constructor provides flexibility to the caller to set the payload itself after sometime
     * @param msgType
     * @throws IOException
     * @throws InterruptedException
     */
    public PayloadMessage(MessageType msgType) throws IOException, InterruptedException
    {
        super(msgType);
    }
    
}
