package cnt5106c.torrent.transceiver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server implements Runnable
{
    private static final int SERVER_PORT = 6789;
    private ServerSocket serverSocket;

    public Server() throws IOException
    {
        this.serverSocket = new ServerSocket(SERVER_PORT, 0, null);
    }
    
    public Server(int port) throws IOException
    {
        // 0 provided for backlog represents default value
        //null for InetAddress means all the address bound to this host will listen on given port
        this.serverSocket = new ServerSocket(port, 0, null);
    }
    
    public Server(String hostName, int port) throws UnknownHostException, IOException
    {
        // 0 provided for backlog represents default value
        this.serverSocket = new ServerSocket(port, 0, InetAddress.getByName(hostName));
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
                //pass this to another thread for further communication
                (new Thread(new Client(aClientSocket))).start();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
