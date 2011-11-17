package cnt5106c.torrent.startup;

import java.io.IOException;
import java.util.Map;

import cnt5106c.torrent.config.BadFileFormatException;
import cnt5106c.torrent.config.ConfigReader;
import cnt5106c.torrent.config.PeerConfig;

public class PeerStarter
{
    private final String currentDirectoryPath = System.getProperty("user.dir");
    private final String peerConfigFileName = currentDirectoryPath + "/PeerInfo.cfg";
    
    public static void main(String[] args) throws BadFileFormatException, IOException
    {
        PeerStarter ps = new PeerStarter();
        ps.startAllPeers();
    }

    public void startAllPeers() throws BadFileFormatException, IOException
    {
        ConfigReader cr = new ConfigReader(peerConfigFileName);
        Map<Integer, PeerConfig> peerInfoMap = cr.getPeerConfigMap();
        for(Integer peerID : peerInfoMap.keySet())
        {
            PeerConfig aConfig = peerInfoMap.get(peerID);
            Runtime.getRuntime().exec("ssh " + aConfig.getHostName() + " cd " + currentDirectoryPath + "; java cnt5106c.torrent.peer.Peer " + peerID);
        }
    }
}
