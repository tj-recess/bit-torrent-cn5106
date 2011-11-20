package cnt5106c.torrent.transceiver;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cnt5106c.torrent.config.CommonConfig;
import cnt5106c.torrent.config.PeerConfig;
import cnt5106c.torrent.messages.ChokeMessage;
import cnt5106c.torrent.messages.UnchokeMessage;
import cnt5106c.torrent.peer.TorrentFile;

public class Transceiver
{
    final int NUM_PREFERRED_NEIGHBORS;
    private final int UNCHOKING_INTERVAL;
    private final int OPTIMISTIC_UNCHOKING_INTERVAL;
    private Map<Integer, PeerConfig> peerInfoMap;
    private int myPeerID;
    private String myHostName;
    private int myListenerPort;
    private ConcurrentHashMap<Integer, Client> peerConnectionMap;
    private TorrentFile myTorrentFile;
    private Set<Integer> interestedNeighbours;
    private List<Integer> allPeerIDList;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Set<Integer> chokedPeersSet = new TreeSet<Integer>();
    //private static final Logger logger = Logger.getLogger(Transceiver.class);
    private static final Logger eventLogger = Logger.getLogger("PeerLogging");

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
    public Transceiver(CommonConfig myCommonConfig, Map<Integer, PeerConfig> peerMap, int myPeerID) throws IOException 
    {
        PeerConfig myConfig = peerMap.get(myPeerID);
        this.myHostName = myConfig.getHostName();
        this.myListenerPort = myConfig.getListeningPort();
        this.peerInfoMap = peerMap;
        this.myPeerID = myPeerID;
        //create torrent file for this config, pass it on to Transceiver
        this.myTorrentFile = new TorrentFile(myPeerID, myCommonConfig, peerMap.keySet(), myConfig.hasCompleteFile());
        this.NUM_PREFERRED_NEIGHBORS = myCommonConfig.getNumPreferredNeighbours();
        this.UNCHOKING_INTERVAL = myCommonConfig.getUnchokingInterval();
        this.OPTIMISTIC_UNCHOKING_INTERVAL = myCommonConfig.getOptimisticUnchokingInterval();
        this.peerConnectionMap = new ConcurrentHashMap<Integer, Client>();
        this.interestedNeighbours = new TreeSet<Integer>();
        allPeerIDList = new ArrayList<Integer>();
        //add all the peerIDs in allPeerIDList
        allPeerIDList.addAll(this.peerInfoMap.keySet());
        //initialize interested neighbors list with allPeerIDsList
        this.interestedNeighbours.addAll(allPeerIDList);
        
        String peerLogFileName = "log_peer_" + myPeerID + ".log";
        // start logger for this peer
        System.setProperty("peer.logfile", peerLogFileName);
        PropertyConfigurator.configure("log4j.properties");
    }
    
    public void start() throws SocketTimeoutException, IOException
    {
        (new Thread(new Server(myHostName, myListenerPort, this))).start();
        eventLogger.info("Started the server on port " + myListenerPort);
        this.processPeerInfoMap();
        scheduler.scheduleAtFixedRate(new PreferredNeighborsManager(this), 0, UNCHOKING_INTERVAL, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new OptimisticNeighborManager(this), 0, OPTIMISTIC_UNCHOKING_INTERVAL, TimeUnit.SECONDS);
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
                //now make an EventHandler (algorithm) for this client
                EventManager anEventManager = new EventManager(newClient, this);
                //start event manager before client starts any activity
                (new Thread(anEventManager)).start();
                this.peerConnectionMap.put(aPeerID, newClient);
                eventLogger.info("Started Client for peerID = " + aPeerID);
            }
        }
    }
    
    public void sendMessageToGroup(List<Integer> peerIDList, byte[] data) throws IOException
    {
        for (Integer aPeerID : peerIDList)
        {
            this.peerConnectionMap.get(aPeerID).send(data);
        }
    }
    
    public void sendMessageToPeer(int peerID, byte[] data) throws IOException
    {
        this.peerConnectionMap.get(peerID).send(data);
    }

    public TorrentFile getTorrentFile()
    {
        return this.myTorrentFile;
    }

    public int getMyPeerID()
    {
        return this.myPeerID;
    }
    
    public void reportInterestedPeer(int peerID)
    {
        this.interestedNeighbours.add(new Integer(peerID));
    }
    
    public void reportNotInterestedPeer(int peerID)
    {
        this.interestedNeighbours.remove(new Integer(peerID));
    }

    /**
     * returns list of all the peers participating in file transfer
     * @return List of peer IDs
     */
    public List<Integer> getAllPeerIDList()
    {
        return allPeerIDList;
    }

    /**
     * Computes list of those peers which don't have any interesting pieces left
     * If all the peers have interesting data, returns a list with size 0
     * @return List of peer IDs which don't have any interesting pieces
     */
    public List<Integer> computeAndGetWastePeersList()
    {
        List<Integer> wastePeersList = new ArrayList<Integer>();
        for(Integer peerID : allPeerIDList)
        {
            if(!myTorrentFile.hasInterestingPiece(peerID))
            {
                wastePeersList.add(peerID);
            }
        }
        return wastePeersList;
    }

    int calculateDownloadRate(Integer peerID)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public Set<Integer> getInterestedNeighbours()
    {
        return this.interestedNeighbours;
    }

    /**
     * Sends choked message to peer, adds it to chokedPeerSet
     * @param peerID
     * @throws InterruptedException 
     * @throws IOException 
     */
    public void reportChokedPeer(Integer peerID) throws IOException, InterruptedException
    {
        //send choke message to this peer
        this.sendMessageToPeer(peerID, new ChokeMessage().getBytes());
        synchronized(this)
        {
            this.chokedPeersSet.add(peerID);
        }
    }

    /**
     * Sends unchoke message to peer and removes it from choked set if it was there
     * @param peerID
     * @throws IOException
     * @throws InterruptedException
     */
    public void reportUnchokedPeer(int peerID) throws IOException, InterruptedException
    {
        //send unchoke message to this peer
        this.sendMessageToPeer(peerID, new UnchokeMessage().getBytes());
        synchronized(this)
        {
            this.chokedPeersSet.remove(peerID);
        }
    }

    public Set<Integer> getChokedPeers()
    {
        return this.chokedPeersSet;
    }
}
