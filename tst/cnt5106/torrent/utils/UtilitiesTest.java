package cnt5106.torrent.utils;

import org.junit.Assert;
import org.junit.Test;

import cnt5106c.torrent.utils.Utilities;


public class UtilitiesTest
{
    @Test
    public void testGetIntegerFromByteArray()
    {
        int i = 4;
        byte[] buffer = Utilities.getBytes(4);
        int returnVal = Utilities.getIntegerFromByteArray(buffer, 0);
        Assert.assertEquals(i, returnVal);
    }
}
