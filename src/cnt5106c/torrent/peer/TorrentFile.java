package cnt5106c.torrent.peer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cnt5106c.torrent.config.CommonConfig;
import cnt5106c.torrent.transceiver.Transceiver;

public class TorrentFile
{
    private final String fileName;
    /**
     * Bitmap stores the file in the following format
     * [7,6,5,4,3,2,1,0][15,14,13,12,11,10,9,8][...]
     */
    private Map<Integer, byte[]> peerIdToPieceBitmap;
    private Map<Integer, AtomicInteger> peerIdToPieceDownloadCount;
    private Set<Integer> piecesRequested;
    private FileHandler myFileHandler;
    private final String myDirectory;
    private final int myPeerID;
    private final int totalPiecesRequired;
    private byte[] finalBitmap;
    private Transceiver myTransceiver;
    
    public TorrentFile(int myPeerID, CommonConfig myCommonConfig, Set<Integer> peerConfigIDs, 
            boolean doIHaveFile, Transceiver myTransceiver) throws IOException
    {
        this.myTransceiver = myTransceiver;
        this.myPeerID = myPeerID;
        this.fileName = myCommonConfig.getFileName();
        this.peerIdToPieceBitmap = new HashMap<Integer, byte[]>();
        this.peerIdToPieceDownloadCount = new ConcurrentHashMap<Integer, AtomicInteger>();
        this.piecesRequested = new HashSet<Integer>();
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
        
        //create the bitmap which will be finally required
        this.finalBitmap = getFinalBitmap(totalPiecesRequired);
        
        //create a dummy file on disk for storage if I don't have a complete file
        if(!doIHaveFile)
        {
            this.myFileHandler.createDummyFile();
        }
        else
        {
            // check first if file actually exists or not
        	// TODO: take action accordingly
        	File tempFile = new File(myDirectory + "/" + myCommonConfig.getFileName());
        	if (!tempFile.exists())
        		myTransceiver.logMessage("File not found by " + myPeerID);
            
            // check if file size matches the file-size specified in Common.cfg
        	// TODO: take action accordingly
            if (tempFile.length() != myCommonConfig.getFileSize())
            	myTransceiver.logMessage("File size is not correct, as found by " + myPeerID);
            
            this.peerIdToPieceBitmap.put(myPeerID, this.finalBitmap); 
        }
    }
    
    private byte[] getFinalBitmap(int totalPiecesRequired)
    {
        //add 1 to all of bits in finalBitmap
        int len = (int)Math.ceil((double)totalPiecesRequired/8);
        byte[] finalBitmap = new byte[len];
        for(int i = 0; i < len; i++)
        {
            finalBitmap[i] = (byte)0xFF;
        }
        int lastBytePieces = totalPiecesRequired & 7;   //totalPiecesRequired & 7 = totalPiecesRequired % 8
        if(lastBytePieces > 0)  //then zero-filling is required
        {
            finalBitmap[len - 1] = (byte)(finalBitmap[len - 1]&0xFF >>> (8 - lastBytePieces));
        }
        return finalBitmap;
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
        if(canIQuit())
        {
            //signal Transceiver which will make every thread quit.
            this.myTransceiver.signalQuit();
        }
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
        if(canIQuit())
        {
            //signal Transceiver which will make every thread quit.
            this.myTransceiver.signalQuit();
        }
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
        for(byte[] aBitmap : this.peerIdToPieceBitmap.values())
        {
            if(!isBitmapFinal(aBitmap))
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * This function evaluates some given bitmap and tells if it's same as what's finally expected when complete
     * file is downloaded.
     * @param aBitmap A bitmap to compare with finalBitmap (should be of same length as finalBitmap)
     * @return true if all the bits are equal to the finalBitmap, false otherwise
     */
    private boolean isBitmapFinal(byte[] aBitmap)
    {
        int len = aBitmap.length;
        for(int i = 0; i < len; i++)
        {
            if((aBitmap[i] ^ finalBitmap[i]) != 0)
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
        List<Integer> possiblePieces = new ArrayList<Integer>();
    	// Randomize logic: Malvika
    	// Peer A has an interesting piece
    	// find all these interesting pieces, which are not yet requested
    	// if(this.piecesRequested.contains(pieceIndex))
    	// pick one out of these randomly
        for(int i = 0; i < len; i++)
        {
            if((0xFF&(int)(myFileBitmap[i] | peerBitmap[i])) >= (0xFF&(int)myFileBitmap[i]))
            {
                for(int j = 0; j < 8; j++)
                {
                    //if peer has the piece and I don't have it, request it
                    if((myFileBitmap[i] & (1 << j)) == 0 && (peerBitmap[i] & (1 << j)) != 0)
                    {
                        int attemptedPieceIndex = i*8 + j;
                        desiredPieceID = findAndLogRequestedPiece(attemptedPieceIndex);
                        if (desiredPieceID != -1)
                        	possiblePieces.add(desiredPieceID);
                    }
                }
            }
        }
        if (possiblePieces.size() != 0)
        {
        	// generate a random number 0 and size of possiblePieces
        	Random rand = new Random();
        	int idx = rand.nextInt(possiblePieces.size());
        	// access that element from the possiblePieces list and return
        	int pieceIndex = possiblePieces.get(idx); 
        	this.piecesRequested.add(pieceIndex);
        	return pieceIndex;
        }
        return -1;
    }

    private synchronized int findAndLogRequestedPiece(int pieceIndex)
    {
        if(this.piecesRequested.contains(pieceIndex))
        {
            return -1;
        }
        
        //add this piece to requested piece list and return index
        //this.piecesRequested.add(pieceIndex);
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
