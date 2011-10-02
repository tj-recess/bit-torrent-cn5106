package cnt5106c.torrent.config;

public class PeerConfig
{
    private int peerId;
    private String hostName;
    private int listeningPort;
    private boolean hasFile;

    public PeerConfig()
    {
    } // default ctor

    public PeerConfig(int peerId, String hostName, int listeningPort,
            boolean hasFile)
    {
        this.peerId = peerId;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFile = hasFile;
    }

    public int getPeerId()
    {
        return peerId;
    }

    public String getHostName()
    {
        return hostName;
    }

    public int getListeningPort()
    {
        return listeningPort;
    }

    public boolean getHasFile()
    {
        return hasFile;
    }

    protected void setPeerId(int peerId)
    {
        this.peerId = peerId;
    }

    protected void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    protected void setListeningPort(int listeningPort)
    {
        this.listeningPort = listeningPort;
    }

    protected void setHasFile(boolean hasFile)
    {
        this.hasFile = hasFile;
    }
}
