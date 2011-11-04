package cnt5106.torrent.utils;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Utilities
{
    private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static ByteArrayOutputStream streamHandle = new ByteArrayOutputStream();
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition borrowedStream = lock.newCondition();
    
    /**
     * This method is used for converting an integer into it's 4 byte representation. 
     * This is a thread safe method.
     * @param i an integer for which byte value is asked
     * @return 4 sized byte array
     */
    public static byte[] getBytes(int i)
    {
        synchronized(Utilities.class)
        {
            baos.reset();
            baos.write(i);
            return baos.toByteArray();
        }
    }
    
    public static synchronized ByteArrayOutputStream getStreamHandle() throws InterruptedException
    {
        lock.lock();
        try
        {
            borrowedStream.await();
            streamHandle.reset();
            return streamHandle;
        }
        finally
        {
            lock.unlock();
        }
    }
    
    public static void returnStreamHandle()
    {
        lock.lock();
        try
        {
            borrowedStream.signal();
        }
        finally
        {
            lock.unlock();
        }
    }
}
