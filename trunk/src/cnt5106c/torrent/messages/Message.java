package cnt5106c.torrent.messages;

import java.io.Serializable;

public abstract class Message implements Serializable {
	protected String message;
	protected static final long serialVersionUID = 1L;
	public Message () 
	{
		
	}
	
	public Message(String msg) 
	{
		// Copy the message
		message = new String(msg);
	}
	public String GetMessage()
	{
		return message;
	}	
};

// Derived classes:
// HandshakeMessage
// ActualMessage
