package cnt5106c.torrent.transceiver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;

/**
 * This class takes decision and action based on the events happening at client's receiving end.
 * @author arpit
 */
public class Algorithm implements Runnable
{
    private Transceiver myTransceiver;
    private Client myClient = null;
    private DataInputStream dis = null;
    
    public Algorithm(Transceiver aTransceiver, Client aClient) throws IOException
    {
        this.myTransceiver = aTransceiver;
        this.myClient = aClient;
        this.dis = new DataInputStream(new PipedInputStream(aClient.getPipedOutputStream()));
    }
    
    @Override
    public void run()
    {
        try
        {
            this.readDataAndTakeAction();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void readDataAndTakeAction() throws IOException
    {
        while(true)
        {
            //TODO : break this loop when done
            
            //rule, always read first 4 bytes (or an int) and then read the number of bytes specified by int
            int msgLength = dis.readInt();
            byte[] msgBuffer = new byte[msgLength];
            dis.read(msgBuffer);
            //TODO : now interpret the message and take action
        }
    }
}
