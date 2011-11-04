package cnt5106c.torrent.messages;

public enum MessageType
{
    choke(0), 
    unchoke(1), 
    interested(2), 
    notInterested(3), 
    have(4), 
    bitfield(5), 
    request(6), 
    piece(7);
    
    private int msgType;

    private MessageType(int msgType)
    {
        this.msgType = msgType;
    }
    
    public int getMessageType()
    {
        return this.msgType;
    }
}
