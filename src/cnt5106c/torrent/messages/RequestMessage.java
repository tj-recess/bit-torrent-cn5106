package cnt5106c.torrent.messages;

import java.io.IOException;

import cnt5106c.torrent.utils.Utilities;

public class RequestMessage extends PayloadMessage
{
    private static final long serialVersionUID = 11L;

    public RequestMessage(int requestedPiece) throws IOException, InterruptedException
    {
        super(MessageType.request, Utilities.getBytes(requestedPiece));
    }
};