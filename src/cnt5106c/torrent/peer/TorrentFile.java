package cnt5106c.torrent.peer;

import java.util.ArrayList;
import java.util.List;

public class TorrentFile
{
    private byte[] myFileBitmap;
    private byte[][] peerPieceBitmap;
    private List<Integer> preferredPeerIDList;
    private int optimisticallyUnchokedPeerID;
    
    public TorrentFile(int fileSize, int pieceSize, int numTotalPeers, int numPreferredPeers)
    {
        int totalPieces = fileSize/pieceSize;
        this.myFileBitmap = new byte[totalPieces];
        this.peerPieceBitmap = new byte[numTotalPeers][totalPieces];
        this.preferredPeerIDList= new ArrayList<Integer>(numPreferredPeers);
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
    
    public byte[][] getPeerPieceBitmap()
    {
        return peerPieceBitmap;
    }
    
    public List<Integer> getPreferredPeerIDList()
    {
        return preferredPeerIDList;
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
