package cnt5106c.torrent.transceiver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import cnt5106c.torrent.utils.Utilities;

public class Client implements Runnable
{
    private static final int TIMEOUT = 5000;    //5 seconds
    private static final int RECEIVE_TIMEOUT = 1000;    //1 second
    private Socket me;
    private DataOutputStream dos;
    private DataInputStream dis;
	private String serverAddress;
	private int serverPort;
	private PipedOutputStream pipedOutputStream = new PipedOutputStream();
    private Transceiver myTransceiver;
    private static final Logger debugLogger = Logger.getLogger("A");
	
    /**
     * This ctor is generally used when this peer initiates the connection with other peers.
     * @param serverAddress address of peer server
     * @param serverPort port of peer server
     * @throws SocketTimeoutException if server couldn't not be contacted within specified time, default = 5 seconds
     * @throws IOException
     */
	public Client(String serverAddress, int serverPort, Transceiver myTransceiver) //throws SocketTimeoutException, IOException
	{
	    this.serverAddress = serverAddress;
	    this.serverPort = serverPort;
	    
	    this.me = new Socket();
	    while(true)
	    {
    	    try
            {
                this.me.connect(new InetSocketAddress(this.serverAddress, this.serverPort), TIMEOUT);
            
        	    this.dos = new DataOutputStream(me.getOutputStream());
                this.dis = new DataInputStream(me.getInputStream());
                break;
            }
    	    catch (IOException e)
            {
                //myTransceiver.logMessage("Connection to Port " + this.serverPort + " failed from " + this.me.getPort(), e);
    	        try{Thread.sleep(500);} catch (InterruptedException e1){/*ignore*/}
            }
	    }
        this.myTransceiver = myTransceiver;
        System.out.println("Client: connected to server now...");
	}
	
	public Client(Socket aSocket, Transceiver myTransceiver) throws IOException
	{
	    this.me = aSocket;
	    this.dos = new DataOutputStream(me.getOutputStream());
        this.dis = new DataInputStream(me.getInputStream());
        this.myTransceiver = myTransceiver;
        System.out.println("Client: connected to server now...");
	}
	
	public void send(byte[] data) throws IOException
	{
	    dos.write(data);
	    dos.flush();
	}
	
	private void receive() throws IOException
	{
	    //always read first 4 bytes, then read equivalent to the length indicated by those 4 bytes
	    byte[] lengthBuffer = new byte[4];
	    dis.readFully(lengthBuffer);
	    int length = Utilities.getIntegerFromByteArray(lengthBuffer, 0);
	    pipedOutputStream.write(Utilities.getBytes(length));
	    
	    //now read the data indicated by length and write it to buffer
	    byte[] buffer = new byte[length];
	    dis.readFully(buffer);
	    pipedOutputStream.write(buffer);
	    pipedOutputStream.flush();
	}
	
	synchronized void receive(int preknownDataLength) throws EOFException, IOException
	{
	    byte[] buffer = new byte[preknownDataLength];
	    //using read fully here to completely download the data before placing it in buffer
	    dis.readFully(buffer);
	    pipedOutputStream.write(buffer);
	}
    
	@Override
    public void run()
    {
	    //keep reading until client dies
	    while(true)
        {
    	    try
            {
                this.receive();
            }
    	    catch (InterruptedIOException iioex)
            {
                //check if we need to continue running or not
    	        if(myTransceiver.getTorrentFile().canIQuit())
    	        {
    	            //close the streams, sockets and quit
                    try
                    {
                        this.closeConnections();
                    }
                    catch (IOException e){/*quit silently*/}
    	            
                    break;
    	        }
            }
            catch (IOException e)
            {
                debugLogger.fatal("Client running on port : " + this.me.getPort() + " got fatal exception!", e);
                break;
            }
        }
    }

    private void closeConnections() throws IOException
    {
        if(this.pipedOutputStream != null)
        {
            this.pipedOutputStream.close();
        }
        if(this.dis != null)
        {
            this.dis.close();
        }
        if(this.dos != null)
        {
            this.dos.close();
        }
        if(this.me != null)
        {
            this.me.close();
        }
    }

    public PipedOutputStream getPipedOutputStream()
    {
        return this.pipedOutputStream;
    }
    
    public void setSoTimeout() throws SocketException
    {
        //set timeout for socket related operations (read)
        this.me.setSoTimeout(RECEIVE_TIMEOUT);
    }
}
