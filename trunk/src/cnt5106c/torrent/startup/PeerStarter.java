package cnt5106c.torrent.startup;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import cnt5106c.torrent.config.BadFileFormatException;
import cnt5106c.torrent.config.ConfigReader;
import cnt5106c.torrent.config.PeerConfig;

public class PeerStarter
{
    private final String currentDirectoryPath = System.getProperty("user.dir");
    private final String peerConfigFileName = currentDirectoryPath + "/PeerInfo.cfg";
    
    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        PeerStarter ps = new PeerStarter();
        try
        {
            ps.startAllPeers();
        } catch (BadFileFormatException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void startAllPeers() throws BadFileFormatException, IOException
    {
        ConfigReader cr = new ConfigReader(peerConfigFileName);
        Map<Integer, PeerConfig> peerInfoMap = cr.getPeerConfigMap();
        for(Integer peerID : peerInfoMap.keySet())
        {
            PeerConfig aConfig = peerInfoMap.get(peerID);
            String cmd = "ssh " + aConfig.getHostName() + " cd " + currentDirectoryPath + "; java -cp ../../log4j-1.2.16.jar:. cnt5106c.torrent.peer.Peer " + peerID;
            System.out.println(cmd);
            Runtime.getRuntime().exec(cmd);
        }
    }
}
