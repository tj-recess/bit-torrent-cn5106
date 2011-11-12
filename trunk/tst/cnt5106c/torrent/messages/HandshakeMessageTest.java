package cnt5106c.torrent.messages;

import static org.junit.Assert.assertEquals;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class HandshakeMessageTest
{ 
    private static final int PEER_ID = 34;
    private HandshakeMessage intMessage = null;
    private HandshakeMessage byteMessage = null;
    
    @Before
    public void setup() throws IOException, InterruptedException
    {
        intMessage = new HandshakeMessage(PEER_ID);
        byteMessage = new HandshakeMessage(intMessage.getBytes());
    }
    
    @Test
    public final void testHandshakeMessageInt()
    {
        Assert.assertNotNull(intMessage);
        //check for length of handshake message too
        Assert.assertEquals(32, intMessage.getBytes().length);
    }

    /**
     * Create a message with PEER_ID, get the bytes, construct a new message from bytes
     * Compare these two objects on Equals.
     * @throws InterruptedException 
     * @throws IOException 
     */
    @Test
    public final void testHandshakeMessageByteArray()
    {
        //try to verify size of both the messages
        Assert.assertEquals(intMessage.getBytes().length, byteMessage.getBytes().length);
        //compare the content of objects
        Assert.assertEquals(intMessage.HANDSHAKE_MSG_HEADER, byteMessage.HANDSHAKE_MSG_HEADER);
        Assert.assertEquals(intMessage.getPeerID(), byteMessage.getPeerID());
    }

    @Test
    public final void testGetPeerID()
    {
        //try to verify the peer ID from msg
        assertEquals(PEER_ID, intMessage.getPeerID());
    }

    @Test
    public final void verifyHeader()
    {
        assertEquals("CEN5501C2008SPRING", intMessage.HANDSHAKE_MSG_HEADER);
        assertEquals("CEN5501C2008SPRING", byteMessage.HANDSHAKE_MSG_HEADER);
    }
}
