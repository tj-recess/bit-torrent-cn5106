package cnt5106c.torrent.config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConfigReader
{
    private BufferedReader fileReader;

    public ConfigReader(String filePath) throws FileNotFoundException
    {
        this.fileReader = null;
        this.tryOpeningFile(filePath);
    }

    private void tryOpeningFile(String filePath) throws FileNotFoundException
    {
        fileReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath)));
    }

    public List<PeerConfig> getPeerConfigList() throws BadFileFormatException,
            IOException
    {
        if (this.fileReader == null)
        {
            throw new BadFileFormatException("Couldn't open the file");
        }
        List<PeerConfig> peerConfigList = new ArrayList<PeerConfig>();
        while (true)
        {
            String aLine = fileReader.readLine();

            if (aLine == null)
                break;

            String[] tokens = aLine.split(" ");
            try
            {
                fillTokens(tokens, peerConfigList);
            } catch (Exception ex)
            {
                throw new BadFileFormatException(ex.getMessage());
            }
        }

        return peerConfigList;
    }

    private void fillTokens(String[] tokens, List<PeerConfig> peersConfigList)
    {
        PeerConfig peerConfig = new PeerConfig();
        peerConfig.setPeerId(Integer.parseInt(tokens[0]));
        peerConfig.setHostName(tokens[1]);
        peerConfig.setListeningPort(Integer.parseInt(tokens[2]));
        peerConfig.setHasFile(Integer.parseInt(tokens[3]) == 1 ? true : false);

        // add this peer to list
        peersConfigList.add(peerConfig);
    }

    public CommonConfig getCommonConfig() throws BadFileFormatException,
            IOException
    {
        if (this.fileReader == null)
        {
            throw new BadFileFormatException("Couldn't open the file");
        }
        CommonConfig commonConfig = new CommonConfig();
        while (true)
        {
            String aLine = fileReader.readLine();

            if (aLine == null)
                break;

            String[] tokens = aLine.split(" ");
            try
            {
                fillTokens(tokens, commonConfig);
            } catch (Exception ex)
            {
                throw new BadFileFormatException(ex.getMessage());
            }
        }

        return commonConfig;
    }

    private void fillTokens(String[] tokens, CommonConfig commonConfig)
    {
        if (tokens[0].equalsIgnoreCase("NumberOfPreferredNeighbors"))
        {
            commonConfig.setNumPreferredNeighbours(Integer.parseInt(tokens[1]));
        } else if (tokens[0].equalsIgnoreCase("UnchokingInterval"))
        {
            commonConfig.setUnchokingInterval(Integer.parseInt(tokens[1]));
        } else if (tokens[0].equalsIgnoreCase("OptimisticUnchokingInterval"))
        {
            commonConfig.setOptimisticUnchokingInterval(Integer
                    .parseInt(tokens[1]));
        } else if (tokens[0].equalsIgnoreCase("FileName"))
        {
            commonConfig.setFileName(tokens[1]);
        } else if (tokens[0].equalsIgnoreCase("FileSize"))
        {
            commonConfig.setFileSize(Integer.parseInt(tokens[1]));
        } else if (tokens[0].equalsIgnoreCase("PieceSize"))
        {
            commonConfig.setPieceSize(Integer.parseInt(tokens[1]));
        } else
        {
            // unknown data in file, ignore
        }
    }
}