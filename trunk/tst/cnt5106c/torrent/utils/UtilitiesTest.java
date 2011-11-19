package cnt5106c.torrent.utils;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;

import cnt5106c.torrent.utils.Utilities;


public class UtilitiesTest
{
    @Test
    public void testGetIntegerFromByteArray()
    {
        int i = 128;
        byte[] buffer = Utilities.getBytes(i);
        int returnVal = Utilities.getIntegerFromByteArray(buffer, 0);
        Assert.assertEquals(i, returnVal);
    }
    
    @Test
    public final void testGetStreamHandle() throws InterruptedException
    {
        ByteArrayOutputStream baos1 = Utilities.getStreamHandle();
        Assert.assertNotNull(baos1);
        Utilities.returnStreamHandle();
    }
}
