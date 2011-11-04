package cnt5106c.torrent.messages;

import java.io.IOException;

import cnt5106.torrent.utils.Utilities;

public abstract class HaveMessage extends PayloadMessage
{
    private static final long serialVersionUID = 9L;

    public HaveMessage(int pieceIndex) throws IOException, InterruptedException
    {
        super(MessageType.have, Utilities.getBytes(pieceIndex));
    }
};
