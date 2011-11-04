package cnt5106c.torrent.messages;

import java.io.Serializable;

public abstract class Message implements Serializable {
	protected byte[] message;
	protected static final long serialVersionUID = 1L;
	
	/**
	 * default constructor to enable subclasses to compute message before setting bytes
	 */
	public Message(){}
	
	public Message(byte[] msg)
	{
		this.message = msg;
	}
	
	public byte[] getBytes()
	{
		return message;
	}	
}

// Derived classes:
// HandshakeMessage
// ActualMessage
