package cnt5106c.torrent.transceiver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;

import cnt5106c.torrent.peer.TorrentFile;

/**
 * This class takes decision and action based on the events happening at client's receiving end.
 * @author arpit
 */
public class EventManager implements Runnable
{
    private TorrentFile myTorrentFile;
    private Client myClient = null;
    private DataInputStream dis = null;
    
    public EventManager(Client aClient, TorrentFile myTorrentFile) throws IOException
    {
        this.myTorrentFile = myTorrentFile;
        this.myClient = aClient;
        this.dis = new DataInputStream(new PipedInputStream(aClient.getPipedOutputStream()));
    }
    
    @Override
    public void run()
    {
        try
        {
            //start the thread for this client to start receiving stuff before sending anything
            (new Thread(myClient)).start();
            //send handshake as connection is already established
            myClient.sendHandshake();
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
