package cnt5106c.torrent.messages;

import java.io.IOException;

public class InterestedMessage extends ActualMessage
{
    private static final long serialVersionUID = 7L;

    public InterestedMessage() throws IOException, InterruptedException
    {
        super(MessageType.interested);
    }

}
