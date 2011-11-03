package cnt5106c.torrent.peer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cnt5106c.torrent.config.CommonConfig;

public class TorrentFile
{
    private final String fileName;
    private byte[] myFileBitmap;
    private Map<Integer, byte[]> peerIdToPieceBitmap;
    private List<Integer> preferredPeerIDList;
    private int optimisticallyUnchokedPeerID;
    
    public TorrentFile(CommonConfig myConfig, Set<Integer> peerConfigIDs)
    {
        this.fileName = myConfig.getFileName();
        int totalPieces = myConfig.getFileSize()/myConfig.getPieceSize();   //assuming they are perfectly divisible
        this.myFileBitmap = new byte[totalPieces];
        this.peerIdToPieceBitmap = new HashMap<Integer, byte[]>();
        this.preferredPeerIDList= new ArrayList<Integer>(peerConfigIDs.size());
        for(Integer aPeerID : peerConfigIDs)
        {
            this.peerIdToPieceBitmap.put(aPeerID, new byte[totalPieces]);
        }
    }
    
    /**
     * This method will update the piece map with the received piece id
     * @param pieceID the number of piece received
     */
    public void addReceivedPieceToBitmap(int pieceID)
    {
        //TODO:
    }

    public byte[] getMyFileBitmap()
    {
        return myFileBitmap;
    }

    public void updatePeerPieceBitmap(int peerID, int pieceID)
    {
        //TODO:
    }
    
    public byte[] getPeerBitmap(int peerID)
    {
        return this.peerIdToPieceBitmap.get(peerID);
    }
    
    public List<Integer> getPreferredPeerIDList()
    {
        return preferredPeerIDList;
    }
    
    public String getFileName()
    {
        return fileName;
    }

    /**
     * This updates the newly selected optimistically unchoked peer
     * @param optimisticallyUnchokedPeerID ID of the peer which is selected as Optimistically Unchoked
     */
    public void setOptimisticallyUnchokedPeerID(int optimisticallyUnchokedPeerID)
    {
        this.optimisticallyUnchokedPeerID = optimisticallyUnchokedPeerID;
    }
    
    /**
     * Get the ID of the peer identified as optimistically unchoked
     * @return peerID of optimistically unchoked peer
     */
    public int getOptimisticallyUnchokedPeerID()
    {
        return optimisticallyUnchokedPeerID;
    }
}
