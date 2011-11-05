package cnt5106c.torrent.peer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cnt5106c.torrent.config.BadFileFormatException;
import cnt5106c.torrent.config.CommonConfig;
import cnt5106c.torrent.config.ConfigReader;
import cnt5106c.torrent.config.PeerConfig;
import cnt5106c.torrent.transceiver.Transceiver;

public class Peer
{
    //lists below are used for running multiple file transfers
    private final String commonConfigFileName = System.getProperty("user.dir") + "/Common.cfg";
    private final String peerConfigFileName = System.getProperty("user.dir") + "/PeerInfo.cfg";
    private Map<String, Transceiver> fileToTransceiverMap;
    private Map<String, Map<Integer, PeerConfig>> fileToPeerConfigMap;
    private Map<String, CommonConfig> fileToCommonConfigMap = null;
    private final int myPeerID;
    
    /**
     * This constructor should be called through some main function which provides peerID as an argument
     * so that peer can identify itself.
     * @param myPeerID ID of the peer itself
     * @throws BadFileFormatException if somebody has tampered with Config files
     * @throws IOException If there is any issue while reading configuration (e.g. access rights, etc.)
     */
    public Peer(int myPeerID) throws BadFileFormatException, IOException
    {
        this.myPeerID = myPeerID;
        this.fileToCommonConfigMap = new HashMap<String, CommonConfig>();
        this.fileToPeerConfigMap = new HashMap<String, Map<Integer,PeerConfig>>();
        this.fileToTransceiverMap = new HashMap<String, Transceiver>();
        this.startup();
    }

    /**
     * This method reads config files, prepare the tracker (peer config) and starts Transceivers for each file.
     * NOTE : this method will require change if we have to deal with concurrent file downloads.
     * @throws BadFileFormatException
     * @throws IOException
     */
    private void startup() throws BadFileFormatException, IOException
    {
        //read Common and Peer config        
        ConfigReader commonConfigReader = new ConfigReader(commonConfigFileName);
        CommonConfig myCommonConfig = commonConfigReader.getCommonConfig();
        this.fileToCommonConfigMap.put(myCommonConfig.getFileName(), myCommonConfig);
        ConfigReader peerConfigReader = new ConfigReader(peerConfigFileName);
        Map<Integer, PeerConfig> peerConfigMap = peerConfigReader.getPeerConfigMap();
        this.fileToPeerConfigMap.put(myCommonConfig.getFileName(), peerConfigMap);
        //create torrent file for this config, pass it on to Transceiver
        TorrentFile aTorrentFile = new TorrentFile(myCommonConfig, peerConfigMap.keySet());
        this.fileToTransceiverMap.put(myCommonConfig.getFileName(), new Transceiver(peerConfigMap, myPeerID, aTorrentFile));
    }
}
