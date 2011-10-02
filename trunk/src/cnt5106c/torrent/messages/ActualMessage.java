package cnt5106c.torrent.messages;

enum eMsgType {choke, unchoke, interested, not_interested, have, bitfield, request, piece}

public abstract class ActualMessage extends Message 
{
	protected static final long serialVersionUID = 2L;
	protected int length;
	//protected String payload;
	protected eMsgType eType;
	
	// Constructors
	public ActualMessage() 
	{
		length = 0;
	}
	public ActualMessage(String msg)
	{
		// Copy the original message
		message = new String(msg);
		
		// Get message length, first 4 bytes
		length = Integer.parseInt(msg.substring(0, 4));
		
		// Get message type, 5th byte
		switch (msg.charAt(4))
		{
		case '0':
			eType = eMsgType.choke;
		break;
		case '1':
			eType = eMsgType.unchoke;
		break;
		case '2':
			eType = eMsgType.interested;
		break;
		case '3':
			eType = eMsgType.not_interested;
		break;
		case '4':
			eType = eMsgType.have;
		break;
		case '5':
			eType = eMsgType.bitfield;
		break;
		case '6':
			eType = eMsgType.request;
		break;
		case '7':
			eType = eMsgType.piece;
		break;
		default:
			// throw error!
		}
		
		/*// These types of messages don't have payload
		if (eType == eMsgType.choke || eType == eMsgType.unchoke ||
			eType == eMsgType.interested || eType == eMsgType.not_interested)
		{
			// No payload
		}
		else if (length != 0) // Get payload, 6th byte onwards
		{
			payload = new String(msg.substring(5, 5+length));
		}*/
	}
	
	public eMsgType GetType()
	{
		return eType;
	}
	
	/*public String GetPayload()
	{
		return payload;
	}*/
	
	public int GetLength()
	{
		return length;
	}
};
