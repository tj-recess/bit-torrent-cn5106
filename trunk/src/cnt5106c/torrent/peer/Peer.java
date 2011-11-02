package cnt5106c.torrent.peer;

import java.util.List;
import java.util.Map;

import cnt5106c.torrent.config.PeerConfig;
import cnt5106c.torrent.transceiver.Transceiver;

public class Peer
{
    //lists below are used for running multiple file transfers
    private List<Transceiver> myTransceivers;
    private List<Map<Integer, PeerConfig>> myPeerConfigs;
    private final int myPeerID;
    
    public Peer(int myPeerID)
    {
        this.myPeerID = myPeerID;
    }
}
