package llc.ufwa.concurrency;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is just a simple string based locking mechanism. Locking/releasing locks across threads is not checked on a per thread basis.
 * So be aware if you lock a thread with one thread then release it with a different thread it WILL release all blocked.
 * @author seanwagner
 *
 */
public class Locks {
    
    private final Map<String, ThreadLock> locksOut = new HashMap<String, ThreadLock>();
   
    public Locks() {
        
    }

    /**
     * 
     * @param lock
     * @throws InterruptedException
     */
    public void getLock(final String lock) throws InterruptedException {
        
        final ThreadLock keysLock;
        
        synchronized(locksOut) {
            
            ThreadLock temp = locksOut.get(lock);
            
            if(temp == null) {
                
                temp = new ThreadLock();
                locksOut.put(lock, temp);
                
            }
            
            keysLock = temp;
            
        }
        
        keysLock.lock();
        
    }
    
    public void releaseLock(final String lock) {
        
        final ThreadLock keysLock;
        
        synchronized(locksOut) {
            keysLock = locksOut.get(lock);
        }
        
        if(keysLock != null) {
            keysLock.unlock();
        }

    }
   
    
    private static class ThreadLock {

        boolean isLocked = false;
        String lockedBy = null;
        int lockedCount = 0;

        public synchronized void lock() throws InterruptedException {

            final Thread callingThread = Thread.currentThread();

            while (isLocked && !lockedBy.equals(callingThread.toString())) {
                wait();
            }

            isLocked = true;
            lockedCount++;
            lockedBy = callingThread.toString();

        }

        public synchronized void unlock() {

            if (Thread.currentThread().toString().equals(this.lockedBy)) {

                lockedCount--;

                if (lockedCount == 0) {

                    isLocked = false;
                    notify();

                }

            }

        }

    }
    
}
