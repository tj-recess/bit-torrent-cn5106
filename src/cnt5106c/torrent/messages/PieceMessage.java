package cnt5106c.torrent.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cnt5106.torrent.utils.Utilities;

public class PieceMessage extends PayloadMessage
{
    private static final long serialVersionUID = 10L;

    public PieceMessage(int pieceIndex, byte[] data) throws IOException, InterruptedException
    {
        super(MessageType.piece);
        ByteArrayOutputStream baos = Utilities.getStreamHandle();
        baos.write(super.message);
        baos.write(Utilities.getBytes(pieceIndex));
        baos.write(data);
        super.message = baos.toByteArray();
        Utilities.returnStreamHandle();
    }
	
};
