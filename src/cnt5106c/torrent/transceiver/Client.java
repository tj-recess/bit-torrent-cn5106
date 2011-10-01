package cnt5106c.torrent.transceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client
{
    private static final int TIMEOUT = 3000;    //3 seconds
    private Socket me;
	private String serverAddress;
	private int serverPort;
	
	public Client(String serverAddress, int serverPort) throws IOException
	{
	    this.serverAddress = serverAddress;
	    this.serverPort = serverPort;
	    
	    me = new Socket();
	    me.connect(new InetSocketAddress(this.serverAddress, this.serverPort), TIMEOUT);
	    System.out.println("Client: connected to server now...");
	}
	
	public Client() throws IOException
	{
	    this("localhost", 6789);
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
}
