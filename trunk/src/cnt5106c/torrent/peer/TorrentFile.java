package cnt5106c.torrent.peer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
    private List<Integer> piecesRequested;
    private List<Integer> interestedNeighbours;
    
    public TorrentFile(CommonConfig myConfig, Set<Integer> peerConfigIDs)
    {
        this.fileName = myConfig.getFileName();
        int totalPieces = myConfig.getFileSize()/myConfig.getPieceSize();   //assuming they are perfectly divisible
        this.myFileBitmap = new byte[totalPieces];
        this.peerIdToPieceBitmap = new HashMap<Integer, byte[]>();
        this.preferredPeerIDList= new ArrayList<Integer>(peerConfigIDs.size());
        this.piecesRequested = new LinkedList<Integer>();
        this.interestedNeighbours = new LinkedList<Integer>();
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

    /**
     * updates the peer's bitmap with the piece id received and returns true if we need this piece
     * @param peerID ID of the peer from which this info has been received (the peer who sent have message)
     * @param pieceIndex the location of piece in the file
     */
    public boolean updatePeerPieceBitmap(int peerID, int pieceIndex)
    {
        byte[] peerFileBitmap = this.peerIdToPieceBitmap.get(peerID);
        int pieceLocation = pieceIndex / 8;
        int bitLocation = pieceIndex & 7;   // = pieceIndex % 8
        peerFileBitmap[pieceLocation] |= (1 << bitLocation);
        if((myFileBitmap[pieceLocation] & (1 << bitLocation)) == 0)  // == 0 means we don't have that piece
        {
            return true;
        }
        return false;
    }
    
    public byte[] getPeerBitmap(int peerID)
    {
        return this.peerIdToPieceBitmap.get(peerID);
    }
    
    public void setPeerBitmap(int peerID, byte[] bitmap)
    {
        this.peerIdToPieceBitmap.put(peerID, bitmap);
    }
    
    public List<Integer> getPreferredPeerIDList()
    {
        return preferredPeerIDList;
    }
    
    public void reportInterestedPeer(int peerID)
    {
        if(!this.interestedNeighbours.contains(peerID))
        {
            this.interestedNeighbours.add(new Integer(peerID));
        }
    }
    
    public void reportNotInterestedPeer(int peerID)
    {
        if(this.interestedNeighbours.contains(peerID))
        {
            this.interestedNeighbours.remove(new Integer(peerID));
        }
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

    public boolean doIHaveAnyPiece()
    {
        final int mask = 0x000000FF;
        final int len = myFileBitmap.length;
        for(int i = 0; i < len; i++)
        {
            if((myFileBitmap[i] & mask) != 0)
            {
                return true;
            }
        }
        return false;
    }

    public boolean hasInterestingPiece(int myPeersID)
    {
        final int len = myFileBitmap.length;
        byte[] peerFileBitmap = this.peerIdToPieceBitmap.get(myPeersID); 
        for(int i = 0; i < len; i++)
        {
            if((myFileBitmap[i] & peerFileBitmap[i]) != 0)
            {
                return true;
            }
        }
        return false;
    }

    public int getRequiredPieceFromPeer(int peerID)
    {
        int desiredPieceID = -1;
        
        //check the first available piece which is not requested, if found add to requested piece list
        byte[] peerBitmap = this.peerIdToPieceBitmap.get(peerID);
        final int len = myFileBitmap.length;
        for(int i = 0; i < len && desiredPieceID == -1; i++)
        {
            if((myFileBitmap[i] & peerBitmap[i]) != 0)
            {
                for(int j = 0; j < 8 && desiredPieceID == -1; j++)
                {
                    //if peer has the piece and I don't have it, request it
                    if((myFileBitmap[i] & (1 << j)) == 0 && (peerBitmap[i] & (1 << j)) == 1)
                    {
                        int attemptedPieceIndex = i*8 + j;
                        desiredPieceID = findAndLogRequestedPiece(attemptedPieceIndex);
                    }
                }
            }
        }
        return desiredPieceID;
    }

    private synchronized int findAndLogRequestedPiece(int pieceIndex)
    {
        if(this.piecesRequested.contains(pieceIndex))
        {
            return -1;
        }
        
        //add this piece to requested piece list and return index
        this.piecesRequested.add(pieceIndex);
        return pieceIndex;
    }
}
