package cnt5106c.torrent.transceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class PreferredNeighborsManager implements Runnable
{
    private Transceiver myTransceiver;
    private Set<Integer> preferredPeerIDSet;

    public PreferredNeighborsManager(Transceiver myTransceiver)
    {
        this.myTransceiver = myTransceiver;
        this.preferredPeerIDSet= new TreeSet<Integer>();
        //this.preferredPeerIDSet.addAll(myTransceiver.getAllPeerIDList());
    }

    public void run()
    {
        try
        {
            selectPreferredNeighbors();
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
     * This method calculates the download rate for all the peers interested in our data.
     * Then it sorts based on the download rate and creates new preferred peers' list.
     * Based on this new list and old list, it decides which peers should be choked or unchoked.
     * @throws InterruptedException 
     * @throws IOException 
     */
    private void selectPreferredNeighbors() throws IOException, InterruptedException
    {
        //calculate download rate from those neighbors which are interested in our data
        List<double[]> peerDownloadRatesList = new ArrayList<double[]>();
        Set<Integer> interestedNeighborsList = myTransceiver.getInterestedNeighbours();
        for(Integer peerID : interestedNeighborsList)
        {
            double[] peerIDAndRatePair = new double[2];
            peerIDAndRatePair[0] = (double)peerID;
            peerIDAndRatePair[1] = myTransceiver.getDownloadRate(peerID);
            peerDownloadRatesList.add(peerIDAndRatePair);
        }
        //now sort the peers based on their download rates
        Collections.sort(peerDownloadRatesList, new Comparator<double[]>()
        {
            @Override
            public int compare(double[] rate1, double[] rate2)
            {
                if(rate1[1] > rate2[1])
                    return 1;
                else if(rate1[1] < rate2[1])
                    return -1;
                else 
                    return 0;
            }
        });
        
        Set<Integer> newPreferredPeersSet = new TreeSet<Integer>();
        int count = peerDownloadRatesList.size() < myTransceiver.NUM_PREFERRED_NEIGHBORS ? 
                peerDownloadRatesList.size() : myTransceiver.NUM_PREFERRED_NEIGHBORS;
                
        //now find preferred peers and take action
        for(int i = 0; i < count; i++)
        {
            int peerID = (int)peerDownloadRatesList.get(i)[0];
            if(!preferredPeerIDSet.contains(peerID))
            {
                //report to transceiver
                myTransceiver.reportUnchokedPeer(peerID);
            }
            //add this client to new preferred list
            newPreferredPeersSet.add(peerID);
        }
        
        //now go through old preferred neighbors and send choke to those who are not preferred now
        for(Integer peerID : this.preferredPeerIDSet)
        {
            if(!newPreferredPeersSet.contains(peerID))
            {
                //report to transceiver
                myTransceiver.reportChokedPeer(peerID);
            }
        }
        
        //finally replace old list with new preferred peers list
        this.preferredPeerIDSet = newPreferredPeersSet;
        
        if(preferredPeerIDSet.size() > 0)
        {
            // print this comma separated list in event logger
            String commaSeparatedList = "";
            for(Integer peerID : this.preferredPeerIDSet)
            {
            	commaSeparatedList += peerID;
            	commaSeparatedList += ",";
            }
            String betterCommaSepString = commaSeparatedList.substring(0, commaSeparatedList.length()-1);
            myTransceiver.logMessage("Peer " + myTransceiver.getMyPeerID() + " has the preferred neighbors " + betterCommaSepString);        
        }
    }
}
