package cnt5106c.torrent.messages;

import java.io.IOException;

public class ChokeMessage extends ActualMessage
{
    private static final long serialVersionUID = 5L;

    public ChokeMessage() throws IOException, InterruptedException
    {
        super(MessageType.choke);
    }
}
