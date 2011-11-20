package cnt5106c.torrent.transceiver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;

import org.apache.log4j.Logger;

import cnt5106c.torrent.messages.ActualMessage;
import cnt5106c.torrent.messages.BitfieldMessage;
import cnt5106c.torrent.messages.HandshakeMessage;
import cnt5106c.torrent.messages.HaveMessage;
import cnt5106c.torrent.messages.InterestedMessage;
import cnt5106c.torrent.messages.Message;
import cnt5106c.torrent.messages.MessageType;
import cnt5106c.torrent.messages.NotInterestedMessage;
import cnt5106c.torrent.messages.PieceMessage;
import cnt5106c.torrent.messages.RequestMessage;
import cnt5106c.torrent.peer.TorrentFile;
import cnt5106c.torrent.utils.Utilities;

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
    private Transceiver myTransceiver;
    private static final Logger debugLogger = Logger.getLogger("A");
    private final String debugHeader;
    
    public EventManager(Client aClient, Transceiver myTransceiver) throws IOException
    {
        this.myTransceiver = myTransceiver;
        this.myTorrentFile = myTransceiver.getTorrentFile();
        this.myClient = aClient;
        this.dis = new DataInputStream(new PipedInputStream(aClient.getPipedOutputStream()));
        this.myOwnID = myTransceiver.getMyPeerID();       
        debugHeader = "Peer " + this.myOwnID + " : ";
    }
    
    @Override
    public void run()
    {
        try
        {
            //send handshake as connection is already established
            this.sendHandshake(myOwnID);
            //now wait for receiving handshake and interpret handshake message
            this.processHandshake(this.receiveHandshake());

            //start a new thread for this client to start receiving stuff before sending anything
            (new Thread(myClient)).start();
            this.readDataAndTakeAction();
        }
        catch (IOException e)
        {
            debugLogger.fatal("IOEx", e);
        } catch (InterruptedException e)
        {
            debugLogger.fatal("InterruptedEx", e);
        }
    }
    
    private HandshakeMessage receiveHandshake() throws IOException
    {
        myClient.receive(32);
        byte[] handshakeMsg = new byte[32];
        dis.read(handshakeMsg);
        debugLogger.info(debugHeader + "Received Handshake message : " + new String(handshakeMsg));
        return new HandshakeMessage(handshakeMsg);
    }

    private void sendHandshake(int myPeerID) throws IOException, InterruptedException
    {
        Message msg = new HandshakeMessage(myPeerID);
        myClient.send(msg.getBytes());
        debugLogger.info(debugHeader + "sent Handshake to peer " + myPeersID);
    }

    private void readDataAndTakeAction() throws IOException, InterruptedException
    {        
        //now always receive bytes and take action
        while(!myTorrentFile.canIQuit())
        {
            debugLogger.debug(debugHeader + "waiting to receive next message");
            ActualMessage msg = getNextMessage();
            //now interpret the message and take action
            takeAction(msg);
        }
    }

    private void takeAction(ActualMessage msg) throws IOException, InterruptedException
    {
        int payloadLength = msg.getMessageLength() - MessageType.getMessageTypeLength();  //removing  the size of message type
        debugLogger.debug(debugHeader + "Received msg of length " + payloadLength);
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
            takeActionForPieceMessage(payloadLength);
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

    /**
     * Algorithm : 
     * 0. report this received piece to Torrent file
     * 1. check if you are still unchoked!
     * 2. if yes, find any random piece which my peer has but I don't have
     * 3. if found, and I am unchoked send request message
     * 4. Notify others of this piece availability by sending have messages to everyone.
     * @throws IOException for any error while reading the data from network
     * @throws InterruptedException 
     */
    private void takeActionForPieceMessage(int msgLength) throws IOException, InterruptedException
    {
        //retrieve the piece from pipe
        //first retrieve the piece index
        int pieceIndex = dis.readInt();
        byte[] pieceData = new byte[msgLength - 4];  //subtracting the length of pieceIndex
        dis.read(pieceData);
        //store this pieceData in file
        myTorrentFile.reportPieceReceived(pieceIndex, pieceData);
        
        // write this action in peer's log file
        myTransceiver.logMessage("Peer " + myOwnID + " has downloaded the piece " + pieceIndex + " from " + myPeersID 
        						 + ". Now the number of pieves it has is " + myTorrentFile.getDownloadedPieceCount(myOwnID));
        
        // check if all pieces have been downloaded
        if (myTorrentFile.getDownloadedPieceCount(myOwnID) == myTorrentFile.getTotalPieceCount())
        	myTransceiver.logMessage("Peer " + myOwnID + "has downloaded the complete file.");
        
        // If not choked, request for more pieces
        if(!amIchoked)
        {
            //send request for another piece
            int desiredPiece = myTorrentFile.getRequiredPieceIndexFromPeer(myPeersID);
            if(desiredPiece != -1)
            {
                myClient.send((new RequestMessage(desiredPiece)).getBytes());
            }
        }
        //send have message to every one to notify about this piece received
        myTransceiver.sendMessageToGroup(myTransceiver.getAllPeerIDList(), (new HaveMessage(pieceIndex)).getBytes());
        //after receiving the piece check if you need to send not-interested message to any of the peers
        myTransceiver.sendMessageToGroup(myTransceiver.computeAndGetWastePeersList(), new NotInterestedMessage().getBytes());
    }

    /**
     * Algorithm:
     * 1. read the requested piece index from pipe 
     * 2. read data from that location in file
     * 3. send the data to peer
     * @throws IOException if there is an exception while read the data from file
     * @throws InterruptedException
     */
    private void takeActionForRequestMessage() throws IOException, InterruptedException
    {        
        //1. we know that requested piece index is an integer
        int pieceIndex = dis.readInt();
        //2. now read this data from file.
        byte[] dataForPiece = myTorrentFile.getPieceData(pieceIndex);
        //3. send this data as piece packet to peer
        myClient.send((new PieceMessage(pieceIndex, dataForPiece)).getBytes());
    }

    private void takeActionForHaveMessage(int msgLength) throws IOException, InterruptedException
    {
        //read payload from pipe
        byte[] payload = new byte[msgLength];
        dis.read(payload);
        //as we know that this payload is piece index, convert it and pass to torrent file
        int pieceIndex = Utilities.getIntegerFromByteArray(payload, 0);
        
        myTransceiver.logMessage("Peer " + myOwnID + " received the 'have' message from " + myPeersID + " for the piece " + pieceIndex);
        myTorrentFile.reportPeerPieceAvailablity(myPeersID, pieceIndex);
        if(!myTorrentFile.doIHavePiece(pieceIndex))
        {
            //report that neighbor is interesting
            myTransceiver.reportInterestedPeer(myPeersID);
            myClient.send((new InterestedMessage()).getBytes());
        }
        else
        {
            myTransceiver.reportNotInterestedPeer(myPeersID);
            myClient.send((new NotInterestedMessage()).getBytes());
        }
    }

    private void takeActionForNotInterestedMessage()
    {
    	myTransceiver.logMessage("Peer " + myOwnID + " received the 'not interested' message from " + myPeersID);
        //remove from interested neighbours list
        myTransceiver.reportNotInterestedPeer(myPeersID);
    }

    private void takeActionForInterestedMessage()
    {
    	myTransceiver.logMessage("Peer " + myOwnID + " received the 'interested' message from " + myPeersID);
        //add to interested neighbors list
        myTransceiver.reportInterestedPeer(myPeersID);
    }

    private void takeActionForUnchokeMessage() throws IOException, InterruptedException
    {
    	myTransceiver.logMessage("Peer " + myOwnID + " is unchoked by " + myPeersID);
        //first of all set the status that I am now unchoked
        this.amIchoked = false;
        //select any piece which my peer has but I don't have and I have not already requested
        int pieceIndex = myTorrentFile.getRequiredPieceIndexFromPeer(myPeersID);
        if(pieceIndex != -1)
        {
            myClient.send((new RequestMessage(pieceIndex)).getBytes());
        }
    }

    private void takeActionForChokeMessage()
    {
    	myTransceiver.logMessage("Peer " + myOwnID + " is choked by " + myPeersID); 
        //set the status that I am choked now
        this.amIchoked = true;
    }

    private void processHandshake(HandshakeMessage handshakeMsg) throws IOException, InterruptedException
    {
        this.myPeersID = handshakeMsg.getPeerID();
        //report this peer connection to Transceiver
        this.myTransceiver.reportNewClientConnection(this.myPeersID, myClient);
        //TODO : check if handshake header is correct or not
        
        myTransceiver.logMessage("Peer " + myOwnID + " is connected from Peer " + myPeersID);
        
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
