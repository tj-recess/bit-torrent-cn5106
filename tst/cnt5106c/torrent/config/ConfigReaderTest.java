package cnt5106c.torrent.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigReaderTest
{
    private static final String COMMON_CONFIG_FILE_PATH = System.getProperty("user.dir") + "/CommonConfig12345.cfg";
    private static final int NUMBER_OF_PREFERRED_NEIGHBOURS = 2;
    private static final int UNCHOKING_INTERVAL = 5;
    private static final int OPTIMISTIC_UNCHOKING_INTERVAL = 15;
    private static final int FILE_SIZE = 10000232;
    private static final int PIECESIZE = 32768;
    private static final String FILE_NAME = "TheFile.dat";
    private static PrintWriter writer;
    
    private static final String PEER_CONFIG_FILE_PATH = System.getProperty("user.dir") + "/PeerConfig12345.cfg";
    private static final int PEER_ID = 1001;
    private static final String HOST_NAME = "sun114-11.cise.ufl.edu";
    private static final int LISTENING_PORT = 6008;
    private static final int HAS_FILE = 1;
    
    @BeforeClass
    public static void setup() throws FileNotFoundException
    {
        createCommonConfigFile();
        createPeerConfigFile();
    }
    
    private static void createPeerConfigFile() throws FileNotFoundException
    {
        writer = new PrintWriter(PEER_CONFIG_FILE_PATH);
        writer.println(PEER_ID + " " + HOST_NAME + " " + LISTENING_PORT + " " + HAS_FILE);
        writer.close();
    }

    private static void createCommonConfigFile() throws FileNotFoundException
    {
        //create a file in current directory with know values
        writer = new PrintWriter(COMMON_CONFIG_FILE_PATH);
        writer.println("NumberOfPreferredNeighbors " + NUMBER_OF_PREFERRED_NEIGHBOURS);
        writer.println("UnchokingInterval " + UNCHOKING_INTERVAL);
        writer.println("OptimisticUnchokingInterval " + OPTIMISTIC_UNCHOKING_INTERVAL);
        writer.println("FileName " + FILE_NAME);
        writer.println("FileSize " + FILE_SIZE);
        writer.println("PieceSize " + PIECESIZE);
        writer.close();
    }

    @Test
    public void getCommonConfigTest() throws BadFileFormatException, IOException
    {
        //If we get the common config object from same file path which is provided in setup
        //it should have same values as given in setup
        
        ConfigReader reader = new ConfigReader(COMMON_CONFIG_FILE_PATH);
        CommonConfig configObj = reader.getCommonConfig();
        if(configObj.getNumPreferredNeighbours() != NUMBER_OF_PREFERRED_NEIGHBOURS
           || configObj.getUnchokingInterval() != UNCHOKING_INTERVAL
           || configObj.getOptimisticUnchokingInterval() != OPTIMISTIC_UNCHOKING_INTERVAL
           || ! configObj.getFileName().equals(FILE_NAME)
           || configObj.getFileSize() != FILE_SIZE
           || configObj.getPieceSize() != PIECESIZE)
        {
            Assert.fail();
        }
    }
    
    @Test(expected=FileNotFoundException.class)
    public void configReaderWithEmptyFileTest() throws FileNotFoundException
    {
        new ConfigReader("");
    }
    
    @Test
    public void getPeerConfigTest() throws BadFileFormatException, IOException
    {
        ConfigReader reader = new ConfigReader(PEER_CONFIG_FILE_PATH);
        Map<Integer, PeerConfig> peerConfig = reader.getPeerConfigList();
        
        if(peerConfig.size() != 1)
            Assert.fail();
        
        PeerConfig aPeer = peerConfig.get(PEER_ID);
        if(!aPeer.getHostName().equals(HOST_NAME)
                || aPeer.getListeningPort() != LISTENING_PORT
                || aPeer.getHasFile() != true)
        {
            Assert.fail();
        }
    }
    
    
}
