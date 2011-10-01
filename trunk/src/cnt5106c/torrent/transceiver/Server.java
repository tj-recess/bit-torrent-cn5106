package cnt5106c.torrent.transceiver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable
{
    private static final int SERVER_PORT = 6789;
    private ServerSocket serverSocket;

    public Server() throws IOException
    {
        this.serverSocket = new ServerSocket(SERVER_PORT, 0, null);
    }

    @Override
    public void run()
    {
        while(true)
        {
            Socket aClient = null;
            // wait for connections forever
            try
            {
                System.out.println("Server is ready to listen now...");
                aClient = this.serverSocket.accept();
                System.out.println("Server received one client request!");
                //pass this to another thread for further communication
                (new Thread(new Communicator(aClient))).start();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
