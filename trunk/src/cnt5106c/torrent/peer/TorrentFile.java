package cnt5106c.torrent.peer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cnt5106c.torrent.config.CommonConfig;

public class TorrentFile
{
    private final String fileName;
    /**
     * Bitmap stores the file in the following format
     * [7,6,5,4,3,2,1,0][15,14,13,12,11,10,9,8][...]
     */
    private Map<Integer, byte[]> peerIdToPieceBitmap;
    private Map<Integer, AtomicInteger> peerIdToPieceDownloadCount;
    private List<Integer> piecesRequested;
    private FileHandler myFileHandler;
    private final String myDirectory;
    private final int myPeerID;
    private final int totalPiecesRequired;
    
    public TorrentFile(int myPeerID, CommonConfig myCommonConfig, Set<Integer> peerConfigIDs, boolean doIHaveFile) throws IOException
    {
        this.myPeerID = myPeerID;
        this.fileName = myCommonConfig.getFileName();
        this.peerIdToPieceBitmap = new HashMap<Integer, byte[]>();
        this.peerIdToPieceDownloadCount = new ConcurrentHashMap<Integer, AtomicInteger>();
        this.piecesRequested = new LinkedList<Integer>();
        this.totalPiecesRequired = (int)Math.ceil((double)myCommonConfig.getFileSize() / myCommonConfig.getPieceSize());
        int totalBytesRequiredForPieces = (int)Math.ceil((double)totalPiecesRequired / 8);
        //initialize maps with all peerIDs (including mine) and 0s in value field
        for(Integer aPeerID : peerConfigIDs)
        {
            this.peerIdToPieceBitmap.put(aPeerID, new byte[totalBytesRequiredForPieces]);
            this.peerIdToPieceDownloadCount.put(aPeerID, new AtomicInteger(0));
        }
        this.myDirectory = System.getProperty("user.dir") + "/peer_" + myPeerID;
        //create a new directory for myself, then create file handler with required file name and pass to TorrentFile
        if(!FileHandler.createDirectoryIfNotExists(myDirectory))
        {
            //TODO : log error, exit
        }
        this.myFileHandler = new FileHandler(myDirectory + "/" + myCommonConfig.getFileName(),
                myCommonConfig.getFileSize(), myCommonConfig.getPieceSize());
        //create a dummy file on disk for storage if I don't have a complete file
        if(!doIHaveFile)
        {
            this.myFileHandler.createDummyFile();
        }
        else
        {
            //TODO : check first if file actually exists or not ????
            //take action accordingly
            //TODO : check if file size matches the file-size specified in Common.cfg
            
            //add 1 to all of your bits in myBitmap
            byte[] myFileBitmap = this.peerIdToPieceBitmap.get(myPeerID); 
            int len = myFileBitmap.length;
            for(int i = 0; i < len; i++)
            {
                myFileBitmap[i] = (byte)0xFF;
            }
            int lastBytePieces = totalPiecesRequired & 7;   //totalPiecesRequired & 7 = totalPiecesRequired % 8
            if(lastBytePieces > 0)  //then zero-filling is required
            {
                myFileBitmap[len - 1] = (byte)(myFileBitmap[len - 1]&0xFF >>> (8 - lastBytePieces));
            }
        }
    }
    
    /**
     * This method will update the piece map with the received piece id and store the piece on disk.
     * This method also maintains a count of how many pieces have been received so far.
     * @param pieceID the number of piece received
     * @throws IOException If there is any issue while write data to disk
     */
    public void reportPieceReceived(int pieceID, byte[] pieceData) throws IOException
    {
        byte[] myFileBitmap = this.peerIdToPieceBitmap.get(myPeerID);
        myFileHandler.writePieceToFile(pieceID, pieceData);
        this.updateBitmapWithPiece(myFileBitmap, pieceID);
        this.peerIdToPieceDownloadCount.get(myPeerID).addAndGet(1);
    }

    public byte[] getMyFileBitmap()
    {
        return this.peerIdToPieceBitmap.get(myPeerID);
    }

    public int getTotalPieceCount()
    {
    	return totalPiecesRequired;
    }
    /**
     * updates the peer's bitmap with the piece id received. This method also updates piece download count for given peerID
     * @param peerID ID of the peer from which this info has been received (the peer who sent have message)
     * @param pieceIndex the location of piece in the file
     */
    public void reportPeerPieceAvailablity(int peerID, int pieceIndex)
    {
        byte[] peerFileBitmap = this.peerIdToPieceBitmap.get(peerID);
        updateBitmapWithPiece(peerFileBitmap, pieceIndex);
        this.peerIdToPieceDownloadCount.get(peerID).addAndGet(1);        
    }
    
    /**
     * This method returns the number of pieces which have been downloaded by any peer so far.
     * @param peerID ID of the peer for which downloaded piece count is asked.
     * @return downloaded piece count
     */
    public int getDownloadedPieceCount(int peerID)
    {
        return this.peerIdToPieceDownloadCount.get(peerID).get();
    }
    
    /**
     * This method checks whether all the pieces have been downloaded by all the peers (including myself) or not.
     * @return true if all the peers have downloaded all the pieces; false otherwise
     */
    public boolean canIQuit()
    {
        for(AtomicInteger numPiecesDownloaded : this.peerIdToPieceDownloadCount.values())
        {
            if(numPiecesDownloaded.get() != totalPiecesRequired)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if the given piece index data is contained by us.
     * @param pieceIndex The piece index which is being checked
     * @return true if we have the piece data, false if we don't
     */
    public boolean doIHavePiece(int pieceIndex)
    {
        byte[] myFileBitmap = this.peerIdToPieceBitmap.get(myPeerID);
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
    
    public String getFileName()
    {
        return fileName;
    }

    public boolean doIHaveAnyPiece()
    {
        byte[] myFileBitmap = this.peerIdToPieceBitmap.get(myPeerID);
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
     * @param anotherPeerID ID of the peer whose bitmap should be checked
     * @return true if peer has any interesting piece, false, if no interesting piece was found
     */
    public boolean hasInterestingPiece(int anotherPeerID)
    {
        byte[] myFileBitmap = this.peerIdToPieceBitmap.get(myPeerID);
        final int len = myFileBitmap.length;
        byte[] peerFileBitmap = this.peerIdToPieceBitmap.get(anotherPeerID); 
        for(int i = 0; i < len; i++)
        {
            //logic : if peer has anything more than us, then ORing the two bitmaps will give more 1s than existing
            //e.g. my = 01101100, peer = 11101100 => my | peer = 11101100. Clearly, (my|peer) > my
            //casting to int and & with 0xFF because 11111111 is treated as -1 if considered byte
            if((0xFF&(int)(myFileBitmap[i] | peerFileBitmap[i])) >= (0xFF&(int)myFileBitmap[i]))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method finds a random interesting piece index which is available from the given peerID
     * @param peerID ID of the peer which has the desired piece
     * @return piece index. If -1, then no interesting piece was found.
     */
    public int getRequiredPieceIndexFromPeer(int peerID)
    {
        byte[] myFileBitmap = this.peerIdToPieceBitmap.get(myPeerID);
        int desiredPieceID = -1;        
        //check the first available piece which is not requested, if found add to requested piece list
        byte[] peerBitmap = this.peerIdToPieceBitmap.get(peerID);
        final int len = myFileBitmap.length;
        for(int i = 0; i < len && desiredPieceID == -1; i++)
        {
            if((myFileBitmap[i] | peerBitmap[i]) > myFileBitmap[i])
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
}