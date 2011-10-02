package cnt5106c.torrent.messages;

public abstract class PieceMessage extends ActualMessage 
{
	protected static final long serialVersionUID = 7L;
	private int pieceIndex;
	private String pieceData;
	private int pieceDataLen;
	
	public PieceMessage (String msg)
	{
		// The payload contains a 4 byte piece index
		pieceIndex = Integer.parseInt(msg.substring(5,9));
		pieceDataLen = length - 4;
		pieceData = new String(msg.substring(9, 9+pieceDataLen));		
	}	
	public int GetPieceIndex()
	{
		return pieceIndex;
	}
	public String GetPieceData()
	{
		return pieceData;
	}
};
