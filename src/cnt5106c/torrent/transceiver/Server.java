package cnt5106c.torrent.transceiver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server implements Runnable
{
    private ServerSocket serverSocket;
    private Transceiver myTransceiver;
    
    public Server(String hostName, int port, Transceiver myTransceiver) throws UnknownHostException, IOException
    {
        // 0 provided for backlog represents default value
        this.serverSocket = new ServerSocket(port, 0, InetAddress.getByName(hostName));
        this.myTransceiver = myTransceiver;
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
                (new Thread(new EventManager(new Client(aClientSocket), myTransceiver))).start();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
