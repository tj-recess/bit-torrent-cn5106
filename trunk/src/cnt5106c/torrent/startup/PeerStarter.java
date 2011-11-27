package cnt5106c.torrent.startup;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cnt5106c.torrent.config.BadFileFormatException;
import cnt5106c.torrent.config.ConfigReader;
import cnt5106c.torrent.config.PeerConfig;

public class PeerStarter
{
    private final String currentDirectoryPath = System.getProperty("user.dir");
    private final String peerConfigFileName = currentDirectoryPath + "/PeerInfo.cfg";
    private static Logger programErrorLogger;
    //private static final Logger programErrorLogger = Logger.getLogger("A");
    
    private enum Mode { run, debug; }
    
    public static void main(String[] args)
    {
        Mode mode = Mode.run;
        if(args.length > 0 && args[0].equals("debug"))
        {
            mode = Mode.debug;
        }
		System.setProperty("peer.logfile", "dummy.log");
        PropertyConfigurator.configure("log4j.properties");
		programErrorLogger = Logger.getLogger("A");

		programErrorLogger.info("Logger started from main");
        PeerStarter ps = new PeerStarter();
        try
        {
            ps.startAllPeers(mode);
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

    public void startAllPeers(Mode modeOfRun) throws BadFileFormatException, IOException
    {
        ConfigReader cr = new ConfigReader(peerConfigFileName);
        Map<Integer, PeerConfig> peerInfoMap = cr.getPeerConfigMap();
        for(Integer peerID : peerInfoMap.keySet())
        {
            PeerConfig aConfig = peerInfoMap.get(peerID);
            String cmd = "";
            if(modeOfRun == Mode.run)
            {
                cmd = "ssh " + aConfig.getHostName() + " cd " + currentDirectoryPath + "; java -cp log4j-1.2.16.jar:. cnt5106c.torrent.peer.Peer " + peerID;
            }
            else
            {
                cmd = "ssh " + aConfig.getHostName() + " cd " + currentDirectoryPath 
                    + "; java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=\"" + (6000 + peerID) + "\" -jar bit-peer.jar " + peerID;
            }
            
            programErrorLogger.info(cmd);
            Runtime.getRuntime().exec(cmd);
        }
    }
}
