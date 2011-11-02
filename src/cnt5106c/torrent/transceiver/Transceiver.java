package cnt5106c.torrent.transceiver;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cnt5106c.torrent.messages.Message;
import cnt5106c.torrent.config.PeerConfig;

public class Transceiver
{
    private Map<Integer, PeerConfig> peerInfoMap;
    private int myPeerID;
    private String myHostName;
    private int myListenerPort;
    private ConcurrentHashMap<Integer, Client> peerConnectionMap;

    /**
     * This ctor starts server immediately in the background on the listening port 
     * and host address provided. It also starts the client connections for the all the peers
     * listed in peer Info map before it's own value.
     * @param myHostName Your own host name
     * @param myListenerPort The port on which server should run
     * @param peerMap A Map of <PeerId, PeerInfo> which has all necessary information to connect to peers
     * @throws UnknownHostException
     * @throws IOException
     */
    public Transceiver(String myHostName, int myListenerPort, Map<Integer, PeerConfig> peerMap, int myPeerID) 
        throws UnknownHostException, IOException
    {
        this.myHostName = myHostName;
        this.myListenerPort = myListenerPort;
        this.peerInfoMap = peerMap;
        this.myPeerID = myPeerID;
        (new Thread(new Server(myHostName, myListenerPort))).start();
        this.peerConnectionMap = new ConcurrentHashMap<Integer, Client>();
        this.processPeerInfoMap();
    }
    
    /**
     * Go through Peer Info Map entrires one by one and then create client for peer IDs < myPeerID.
     * Once a client is created, send handshake immediately.
     * @throws IOException If there is an error while connecting with other clients.
     * @throws SocketTimeoutException If server couldn't be contacted with stipulated time.
     */
    private void processPeerInfoMap() throws SocketTimeoutException, IOException
    {
        for(Integer aPeerID : this.peerInfoMap.keySet())
        {
            if(aPeerID < myPeerID)
            {
                //peer has already been started, try to make a connection
                Client newClient = new Client(this.peerInfoMap.get(aPeerID).getHostName(),
                        this.peerInfoMap.get(aPeerID).getListeningPort());
                newClient.sendHandshake();
                this.peerConnectionMap.put(aPeerID, newClient);
            }
        }
    }

    /**
     * This ctor starts server immediately in the background on the listening port supplied and localhost
     * @param myListenerPort The port on which server should run
     * @param peerInfoMap A Map of <PeerId, PeerInfo> which has all necessary information to connect to peers
     * @throws UnknownHostException
     * @throws IOException
     */
    public Transceiver(int myListenerPort, Map<Integer, PeerConfig> peerInfoMap, int myPeerID) 
        throws UnknownHostException, IOException
    {
        this("localhost", myListenerPort, peerInfoMap, myPeerID);
    }
    
    /**
     * sends a Message object (bytes) to peer on a TCP connection indicated by the peerId
     * @param peerId The ID of peer as specified in PeerConfig.cfg file
     * @param msg Message object which will be sent to peer
     * @throws IOException If there is any issue with sending the data over network
     */
    public void sendMessage(int peerId, Message msg) throws IOException
    {
        Client client = this.peerConnectionMap.get(new Integer(peerId));
        if (client == null)
        {
            PeerConfig peer = this.peerInfoMap.get(new Integer(peerId));
            client = new Client(peer.getHostName(), peer.getListeningPort());
            this.peerConnectionMap.put(new Integer(peerId), client);
        }
        //TODO: send message via client
        
    }
    
    public Message receiveMessage()
    {
        //TODO return message to sender
        return null;
    }
}
