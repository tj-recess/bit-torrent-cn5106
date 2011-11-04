package cnt5106c.torrent.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cnt5106.torrent.utils.Utilities;

public abstract class ActualMessage extends Message
{
	protected static final long serialVersionUID = 2L;
	
	public ActualMessage(MessageType msgType) throws IOException, InterruptedException
	{
		ByteArrayOutputStream baos = Utilities.getStreamHandle();
		baos.write(4);    //length of msg type
		baos.write(msgType.getMessageType());
		super.message = baos.toByteArray();
		Utilities.returnStreamHandle();
	}
};
