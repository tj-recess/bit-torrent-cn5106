package cnt5106c.torrent.transceiver;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class OptimisticNeighborManager implements Runnable
{
    private Transceiver myTransceiver;
    
    public OptimisticNeighborManager(Transceiver transceiver)
    {
        this.myTransceiver = transceiver;
    }

    @Override
    public void run()
    {
        try
        {
            this.selectOptimisticallyUnchokedPeer();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Randomly selects one peer which is interested but not preferred (or choked) currently
     * and unchokes it.
     * @throws InterruptedException 
     * @throws IOException 
     */
    private void selectOptimisticallyUnchokedPeer() throws IOException, InterruptedException
    {
    	// Malvika: TODO: choke the previously optimistically unchoked peer
    	// Here? or the end of the function?
    	// If here, it can be picked again
    	// If later, if this is the only one, it won't get picked!
    	// myTransceiver.reportChokedPeer(myTransceiver.getPrevOptUnchokedPeer());
    	
        //get interested peers and choked peers
        Set<Integer> chokedPeersSet = myTransceiver.getChokedPeers();
        List<Integer> interestedAndChoked = new LinkedList<Integer>();
        interestedAndChoked.addAll(myTransceiver.getInterestedNeighbours());
        //find 1 random peer from the intersection of interested and preferred
        interestedAndChoked.retainAll(chokedPeersSet);
        if(interestedAndChoked.size() > 0)
        {
            Random rand = new Random();
            int selectedPeer = interestedAndChoked.get(rand.nextInt(interestedAndChoked.size()));
            //send unchoke message to this random peer
            myTransceiver.reportUnchokedPeer(selectedPeer);
            // Set this peer in transceiver, so it can be choked the next time
            myTransceiver.setPrevOptUnchokedPeer(selectedPeer);
            // Log this event
            myTransceiver.logMessage("Peer " + myTransceiver.getMyPeerID() + " has optimistically unchoked neighbor " + selectedPeer);
        }
    }

}
