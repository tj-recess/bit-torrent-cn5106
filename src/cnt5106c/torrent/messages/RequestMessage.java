package cnt5106c.torrent.messages;

public abstract class RequestMessage extends ActualMessage
{
	protected static final long serialVersionUID = 6L;
	private int pieceIndex;
	public RequestMessage (String msg)
	{
		// The payload contains a 4 byte piece index
		pieceIndex = Integer.parseInt(msg.substring(5,9));
	}
	public int GetPieceIndex()
	{
		return pieceIndex;
	}
};