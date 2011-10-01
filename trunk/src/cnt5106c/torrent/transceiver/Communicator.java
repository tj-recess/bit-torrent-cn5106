package cnt5106c.torrent.transceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Communicator implements Runnable
{
    private Socket clientSocket;
    
    public Communicator(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run()
    {
        try
        {
            this.talkOnSocket();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            if(clientSocket != null)
            {
                try
                {
                    clientSocket.close();
                } catch (IOException e)
                {
                    //ignore
                }
            }
        }
    }

    private void talkOnSocket() throws IOException
    {
        if(clientSocket == null)
        {
            //TODO log some error and return
            return;
        }
        //get input and output streams and communicate
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        while(true)
        {
            String msg = reader.readLine();
            if(msg.equals("bye"))
            {
                break;
            }
            //echo the message
            writer.println(msg);
        }
    }
}
