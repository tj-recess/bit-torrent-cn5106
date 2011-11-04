package cnt5106c.torrent.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class HandshakeMessage extends Message
{
	protected static final long serialVersionUID = 2L;
	private static final String HANDSHAKE_MSG_HEADER = "CEN5501C2008SPRING";
	
	public HandshakeMessage(int peerID) throws IOException
	{
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    baos.write(HANDSHAKE_MSG_HEADER.getBytes());
	    baos.write(new byte[10]);  //10 bytes zero bits
	    baos.write(peerID);
	}
};
