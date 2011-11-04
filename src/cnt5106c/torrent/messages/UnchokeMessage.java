package cnt5106c.torrent.messages;

import java.io.IOException;

public class UnchokeMessage extends ActualMessage
{
    private static final long serialVersionUID = 6L;

    public UnchokeMessage() throws IOException, InterruptedException
    {
        super(MessageType.unchoke);
    }
}
