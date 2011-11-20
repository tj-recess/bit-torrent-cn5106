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
        //get interested peers and choked peers
        Set<Integer> chokedPeersSet = myTransceiver.getChokedPeers();
        List<Integer> interestedAndChoked = new LinkedList<Integer>();
        interestedAndChoked.addAll(myTransceiver.getInterestedNeighbours());
        //find 1 random peer from the intersection of interested and preferred
        interestedAndChoked.retainAll(chokedPeersSet);
        Random rand = new Random();
        int selectedPeer = interestedAndChoked.get(rand.nextInt(interestedAndChoked.size()));
        //send unchoke message to this random peer
        myTransceiver.reportUnchokedPeer(selectedPeer);
        // Log this event
        myTransceiver.logMessage("Peer " + myTransceiver.getMyPeerID() + " has optimistically unchoked neighbor " + selectedPeer);
    }

}
