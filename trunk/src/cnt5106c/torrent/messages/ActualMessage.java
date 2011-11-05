package cnt5106c.torrent.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cnt5106.torrent.utils.Utilities;

public class ActualMessage extends Message
{
	protected static final long serialVersionUID = 2L;
	private MessageType msgType = null;
    private int msgLength;
	
	public ActualMessage(MessageType msgType) throws InterruptedException, IOException
	{
		this(Integer.SIZE, msgType);
	}
	
	public ActualMessage(int msgLength, MessageType msgType) throws InterruptedException, IOException
	{
	    this.msgType = msgType;
        this.msgLength = msgLength;
        ByteArrayOutputStream baos = Utilities.getStreamHandle();
        baos.write(Utilities.getBytes(this.msgLength));
        baos.write(Utilities.getBytes(msgType.getMessageType()));
        super.message = baos.toByteArray();
        Utilities.returnStreamHandle();
	}

    public MessageType getMsgType()
    {
        return msgType;
    }
    
    public int getMessageLength()
    {
        return this.msgLength;
    }
}
