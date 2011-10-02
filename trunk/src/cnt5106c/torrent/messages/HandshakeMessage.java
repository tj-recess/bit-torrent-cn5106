package cnt5106c.torrent.messages;

public abstract class HandshakeMessage extends Message
{
	protected static final long serialVersionUID = 2L;
	private int peerId;
	
	public HandshakeMessage(String msg)
	{
		message = new String(msg);
		peerId = Integer.parseInt(msg.substring(17,31));
	}
	public int GetPeerId()
	{
		return peerId;
	}
};
