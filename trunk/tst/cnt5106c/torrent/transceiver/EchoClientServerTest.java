package cnt5106c.torrent.transceiver;

import java.io.IOException;

import org.junit.Test;

public class EchoClientServerTest
{
    @Test
    public void test() throws IOException
    {
        Server server = new Server();
        (new Thread(server)).start();
        
        Client client = new Client();
        client.talkOnSocket();
    }
}
