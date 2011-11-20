package cnt5106c.torrent.transceiver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Client implements Runnable
{
    private static final int TIMEOUT = 5000;    //5 seconds
    private Socket me;
    private DataOutputStream dos;
    private DataInputStream dis;
	private String serverAddress;
	private int serverPort;
    private PipedOutputStream pipedOutputStream = new PipedOutputStream();
	
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
	    int length = dis.readInt();
	    pipedOutputStream.write(length);
	    
	    //now read the data indicated by length and write it to buffer
	    byte[] buffer = new byte[length];
	    dis.readFully(buffer);
	    pipedOutputStream.write(buffer);
	}
	
	void receive(int preknownDataLength) throws EOFException, IOException
	{
	    byte[] buffer = new byte[preknownDataLength];
	    //using read fully here to completely download the data before placing it in buffer
	    dis.readFully(buffer);
	    pipedOutputStream.write(buffer);
	}
	
	public void talkOnSocket() throws IOException
	{
	    if(me == null)
	    {
	        //TODO log error and return
	        return;
	    }
	    
	    try
	    {
    	    BufferedReader socketReader = new BufferedReader(new InputStreamReader(me.getInputStream()));
    	    PrintWriter socketWriter = new PrintWriter(me.getOutputStream(), true);
    	    BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
    	    while(true)
    	    {
    	        String input = userInputReader.readLine();
    	        socketWriter.println(input);
    	        String response = socketReader.readLine();
    	        System.out.println("Server says: " + response);
    	        if(input.equalsIgnoreCase("bye"))
    	        {
    	            break;
    	        }
    	    }
	    }
	    finally
	    {
	        if(me != null)
	            me.close();
	    }
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
