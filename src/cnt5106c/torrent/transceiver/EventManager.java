package cnt5106c.torrent.transceiver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;

import cnt5106.torrent.utils.Utilities;
import cnt5106c.torrent.messages.ActualMessage;
import cnt5106c.torrent.messages.BitfieldMessage;
import cnt5106c.torrent.messages.HandshakeMessage;
import cnt5106c.torrent.messages.InterestedMessage;
import cnt5106c.torrent.messages.Message;
import cnt5106c.torrent.messages.MessageType;
import cnt5106c.torrent.messages.NotInterestedMessage;
import cnt5106c.torrent.messages.RequestMessage;
import cnt5106c.torrent.peer.TorrentFile;

/**
 * This class takes decision and action based on the events happening at client's receiving end.
 * @author arpit
 */
public class EventManager implements Runnable
{
    private TorrentFile myTorrentFile;
    private Client myClient = null;
    private DataInputStream dis = null;
    private int myOwnID;
    private int myPeersID = -1;
    private boolean amIchoked = true;
    
    public EventManager(Client aClient, TorrentFile myTorrentFile, int myOwnID) throws IOException
    {
        this.myTorrentFile = myTorrentFile;
        this.myClient = aClient;
        this.dis = new DataInputStream(new PipedInputStream(aClient.getPipedOutputStream()));
        this.myOwnID = myOwnID;
    }
    
    @Override
    public void run()
    {
        try
        {
            //start the thread for this client to start receiving stuff before sending anything
            (new Thread(myClient)).start();
            this.readDataAndTakeAction();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private HandshakeMessage receiveHandshake() throws IOException
    {
        myClient.receive(32);
        byte[] handshakeMsg = new byte[32];
        dis.read(handshakeMsg);
        return new HandshakeMessage(handshakeMsg);
    }

    private void sendHandshake(int myPeerID) throws IOException, InterruptedException
    {
        Message msg = new HandshakeMessage(myPeerID);
        myClient.send(msg.getBytes());
    }

    private void readDataAndTakeAction() throws IOException, InterruptedException
    {
        //send handshake as connection is already established
        this.sendHandshake(myOwnID);
        //now wait for receiving handshake and interpret handshake message
        this.processHandshake(this.receiveHandshake());
        
        //now always receive bytes and take action
        while(true)
        {
            //TODO : break this loop when done
            
            ActualMessage msg = getNextMessage();
            //TODO : now interpret the message and take action
            takeAction(msg);
        }
    }

    private void takeAction(ActualMessage msg) throws IOException, InterruptedException
    {
        int payloadLength = msg.getMessageLength() - Integer.SIZE;  //removing  the size of message type
        switch(msg.getMsgType())
        {
        case choke:
            takeActionForChokeMessage();
            break;
        case unchoke:
            takeActionForUnchokeMessage();
            break;
        case interested:
            takeActionForInterestedMessage();
            break;
        case notInterested:
            takeActionForNotInterestedMessage();
            break;
        case have:
            takeActionForHaveMessage(payloadLength);
            break;
        case request:
            takeActionForRequestMessage();
            break;
        case piece:
            takeActionForPieceMessage();
            break;
        case bitfield:
            takeActionForBitFieldMessage(payloadLength);
        }
    }

    private void takeActionForBitFieldMessage(int msgLength) throws IOException, InterruptedException
    {
        //update the bit field for myPeersID
        //read bitmap from data input stream
        byte[] bitmap = new byte[msgLength];
        dis.read(bitmap);
        myTorrentFile.setPeerBitmap(myPeersID, bitmap);
        
        //check if interested message should be sent or not interested
        if(myTorrentFile.hasInterestingPiece(myPeersID))
        {
            myClient.send((new InterestedMessage()).getBytes());
        }
        else
        {
            myClient.send((new NotInterestedMessage()).getBytes());
        }
    }

    private void takeActionForPieceMessage()
    {
        // TODO Auto-generated method stub
        
        //Algorithm : 
        //1. check if you are still unchoked!
        //2. if yes, find any random piece which my peer has but I don't have
        //3. if found, send request message
        //4. if piece not found or we are choked again, quit

        
        //after receiving the piece check if you need to send not-interested message to any of the peers
        
    }

    private void takeActionForRequestMessage()
    {
        // TODO Auto-generated method stub
        
    }

    private void takeActionForHaveMessage(int msgLength) throws IOException, InterruptedException
    {
        //read payload from pipe
        byte[] payload = new byte[msgLength];
        dis.read(payload);
        //as we know that this payload is piece index, convert it and pass to torrent file
        int pieceIndex = Utilities.getIntegerFromByteArray(payload, 0);
        
        if(myTorrentFile.updatePeerPieceBitmap(myPeersID, pieceIndex))
        {
            myClient.send((new InterestedMessage()).getBytes());
        }
        else
        {
            myClient.send((new NotInterestedMessage()).getBytes());
        }
    }

    private void takeActionForNotInterestedMessage()
    {
        // TODO Auto-generated method stub
        //remove from interested neighbours list
    }

    private void takeActionForInterestedMessage()
    {
        // TODO Auto-generated method stub
        //add to interested neighbours list
    }

    private void takeActionForUnchokeMessage() throws IOException, InterruptedException
    {
        //select any piece which my peer has but I don't have and I have not already requested
        int pieceIndex = myTorrentFile.getRequiredPieceFromPeer(myPeersID);
        myClient.send((new RequestMessage(pieceIndex)).getBytes());
    }

    private void takeActionForChokeMessage()
    {
        // TODO Auto-generated method stub
        
    }

    private void processHandshake(HandshakeMessage handshakeMsg) throws IOException, InterruptedException
    {
        this.myPeersID = handshakeMsg.getPeerID();
        //send my bitmap to others only if I have some piece
        if(myTorrentFile.doIHaveAnyPiece())
        {
            myClient.send((new BitfieldMessage(myTorrentFile.getMyFileBitmap())).getBytes());
        }
    }
    
    private ActualMessage getNextMessage() throws IOException, InterruptedException
    {
        //rule, always read first 4 bytes (or an int) and 
        //then read the message type
        int msgLength = dis.readInt();
        int msgType = dis.readInt();
        return new ActualMessage(msgLength, MessageType.getMessageType(msgType));
    }
}
