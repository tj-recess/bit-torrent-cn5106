package cnt5106c.torrent.transceiver;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class Server implements Runnable
{
    private ServerSocket serverSocket;
    private Transceiver myTransceiver;
    private static final Logger acitivityLogger = Logger.getLogger("A");
    private static final int ACCEPT_TIMEOUT = 1000;
    
    public Server(String hostName, int port, Transceiver myTransceiver) throws UnknownHostException, IOException
    {
        // 0 provided for backlog represents default value
        this.serverSocket = new ServerSocket(port, 0, InetAddress.getByName(hostName));
        this.serverSocket.setSoTimeout(ACCEPT_TIMEOUT);
        this.myTransceiver = myTransceiver;
        Server.acitivityLogger.info("Server is ready to listen now at address : " 
                + this.serverSocket.getLocalSocketAddress().toString() + " and port : " + this.serverSocket.getLocalPort());
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
                aClientSocket = this.serverSocket.accept();
                Server.acitivityLogger.info("Server received a client request!");
                //start an Event manager in another thread for further communication
                (new Thread(new EventManager(new Client(aClientSocket), myTransceiver))).start();
            }
            catch(InterruptedIOException iioex)
            {
                //check if it's OK to quit, if yes, close socket connection
                if(myTransceiver.getTorrentFile().canIQuit())
                {
                    try
                    {
                        this.serverSocket.close();
                        break;
                    } catch (IOException e){}
                }
            }
            catch (IOException e)
            {
                Server.acitivityLogger.fatal("IOEx in Server", e);
            }
        }
    }
}
