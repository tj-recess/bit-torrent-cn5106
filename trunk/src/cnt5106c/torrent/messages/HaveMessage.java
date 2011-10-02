package cnt5106c.torrent.messages;

public abstract class HaveMessage extends ActualMessage
{
	protected static final long serialVersionUID = 4L;
	private int pieceIndex;
	public HaveMessage (String msg)
	{
		// The payload contains a 4 byte piece index
		pieceIndex = Integer.parseInt(msg.substring(5,9));
	}
	public int GetPieceIndex()
	{
		return pieceIndex;
	}
};
