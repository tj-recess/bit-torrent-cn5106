package cnt5106c.torrent.messages;

import java.io.IOException;

public class NotInterestedMessage extends ActualMessage
{
    private static final long serialVersionUID = 8L;

    public NotInterestedMessage() throws IOException, InterruptedException
    {
        super(MessageType.notInterested);
    }
}
