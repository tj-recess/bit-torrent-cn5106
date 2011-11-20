package cnt5106c.torrent.transceiver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import cnt5106c.torrent.utils.Utilities;

public class Client implements Runnable
{
    private static final int TIMEOUT = 5000;    //5 seconds
    private Socket me;
    private DataOutputStream dos;
    private DataInputStream dis;
	private String serverAddress;
	private int serverPort;
	private PipedOutputStream pipedOutputStream = new PipedOutputStream();
	private DataOutputStream pipeDos = new DataOutputStream(pipedOutputStream); 
    
	
    /**
     * This ctor is generally used when this peer initiates the connection with other peers.
     * @param serverAddress address of peer server
     * @param serverPort port of peer server
     * @throws SocketTimeoutException if server couldn't not be contacted within specified time, default = 5 seconds
     * @throws IOException
     */
	public Client(String serverAddress, int serverPort) throws SocketTimeoutException, IOException
	{
	    this.serverAddress = serverAddress;
	    this.serverPort = serverPort;
	    
	    this.me = new Socket();
	    this.me.connect(new InetSocketAddress(this.serverAddress, this.serverPort), TIMEOUT);
	    this.dos = new DataOutputStream(me.getOutputStream());
        this.dis = new DataInputStream(me.getInputStream());
        System.out.println("Client: connected to server now...");
	}
	
	public Client(Socket aSocket) throws IOException
	{
	    this.me = aSocket;
	    this.dos = new DataOutputStream(me.getOutputStream());
        this.dis = new DataInputStream(me.getInputStream());
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
	    pipeDos.writeInt(length);
	    
	    //now read the data indicated by length and write it to buffer
	    byte[] buffer = new byte[length];
	    dis.readFully(buffer);
	    pipeDos.write(buffer);
	}
	
	void receive(int preknownDataLength) throws EOFException, IOException
	{
	    byte[] buffer = new byte[preknownDataLength];
	    //using read fully here to completely download the data before placing it in buffer
	    dis.readFully(buffer);
	    pipeDos.write(buffer);
	}
    
	@Override
    public void run()
    {
	    //keep reading until client dies
	    //TODO : know when to stop
        try
        {
            while(true)
            {
                this.receive();
            }
        } 
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public PipedOutputStream getPipedOutputStream()
    {
        return this.pipedOutputStream;
    }
}
