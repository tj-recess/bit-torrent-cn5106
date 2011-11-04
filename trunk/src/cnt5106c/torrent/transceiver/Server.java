package cnt5106c.torrent.transceiver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import cnt5106c.torrent.peer.TorrentFile;

public class Server implements Runnable
{
    private ServerSocket serverSocket;
    private TorrentFile myTorrentFile;
    
    public Server(String hostName, int port, TorrentFile myTorrentFile) throws UnknownHostException, IOException
    {
        // 0 provided for backlog represents default value
        this.serverSocket = new ServerSocket(port, 0, InetAddress.getByName(hostName));
        this.myTorrentFile = myTorrentFile;
    }

    @Override
    public void run()
    {
        while(true)
        {
            Socket aClientSocket = null;
            // wait for connections forever
            try
            {
                System.out.println("Server is ready to listen now...");
                aClientSocket = this.serverSocket.accept();
                System.out.println("Server received a client request!");
                //start an Event manager in another thread for further communication
                (new Thread(new EventManager(new Client(aClientSocket), myTorrentFile))).start();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
