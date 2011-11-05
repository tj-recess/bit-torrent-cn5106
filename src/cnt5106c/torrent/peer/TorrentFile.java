package cnt5106c.torrent.peer;

import java.io.IOException;
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
    private FileHandler myFileHandler;
    private List<Integer> allPeerIDList;
    
    public TorrentFile(CommonConfig myConfig, Set<Integer> peerConfigIDs, FileHandler myFileHandler)
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
        this.myFileHandler = myFileHandler;
        allPeerIDList = new ArrayList<Integer>();
        allPeerIDList.addAll(this.peerIdToPieceBitmap.keySet());
    }
    
    /**
     * This method will update the piece map with the received piece id, store the piece on disk
     * @param pieceID the number of piece received
     * @throws IOException If there is any issue while write data to disk
     */
    public void reportPieceReceived(int pieceID, byte[] pieceData) throws IOException
    {
        myFileHandler.writePieceToFile(pieceID, pieceData);
        this.updateBitmapWithPiece(myFileBitmap, pieceID);
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
    public void reportPeerPieceAvailablity(int peerID, int pieceIndex)
    {
        byte[] peerFileBitmap = this.peerIdToPieceBitmap.get(peerID);
        updateBitmapWithPiece(peerFileBitmap, pieceIndex);
    }
    
    /**
     * Checks if the given piece index data is contained by us.
     * @param pieceIndex The piece index which is being checked
     * @return true if we have the piece data, false if we don't
     */
    public boolean doIHavePiece(int pieceIndex)
    {
        int pieceLocation = pieceIndex / 8;
        int bitLocation = pieceIndex & 7;   // = pieceIndex % 8
        if((myFileBitmap[pieceLocation] & (1 << bitLocation)) != 0)  // == 0 means we don't have that piece
        {
            return true;
        }
        return false;
    }

    private void updateBitmapWithPiece(byte[] peerFileBitmap, int pieceIndex)
    {
        int pieceLocation = pieceIndex / 8;
        int bitLocation = pieceIndex & 7;   // = pieceIndex % 8
        peerFileBitmap[pieceLocation] |= (1 << bitLocation);
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

    /**
     * Computes if there is any interesting piece with peerID mentioned
     * @param myPeersID ID of the peer whose bitmap should be checked
     * @return true if peer has any interesting piece, false, if no interesting piece was found
     */
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

    public int getRequiredPieceIndexFromPeer(int peerID)
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

    /**
     * returns the data from the file on disk indicated by pieceIndex
     * @param pieceIndex
     * @return actual data from disk
     * @throws IOException
     */
    public byte[] getPieceData(int pieceIndex) throws IOException
    {
        return myFileHandler.getPieceFromFile(pieceIndex);
    }

    /**
     * returns list of all the peers participating in file transfer
     * @return List of peer IDs
     */
    public List<Integer> getAllPeerIDList()
    {
        return allPeerIDList;
    }

    /**
     * Computes list of those peers which don't have any interesting pieces left
     * If all the peers have interesting data, returns a list with size 0
     * @return List of peer IDs which don't have any interesting pieces
     */
    public List<Integer> getWastePeersList()
    {
        List<Integer> wastePeersList = new ArrayList<Integer>();
        for(Integer peerID : allPeerIDList)
        {
            if(hasInterestingPiece(peerID))
            {
                wastePeersList.add(peerID);
            }
        }
        return wastePeersList;
    }
}
