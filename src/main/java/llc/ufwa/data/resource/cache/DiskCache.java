package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import llc.ufwa.concurrency.Locks;
import llc.ufwa.data.exception.ResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Suitable for small files. Thumbnail images. (byte [])
 * 
 * @author seanwagner
 *
 * @param <TKey>
 */
public final class DiskCache<TKey> implements Cache<TKey, byte []> {
	
	private static final Logger logger = LoggerFactory.getLogger(DiskCache.class);
	
	//Immutables
	private final TreeSet<File> sortedFiles = new TreeSet<File>(
        
	    new Comparator<File>() {

            @Override
            public int compare(File item1, File item2) {
                return (int)(item2.lastModified() - item1.lastModified());                
            }
        
        }
        
    );
	
	private final Locks fileLocks = new Locks();
	private final File parent;    
	private final long expiresTimeout;
	private final States states = new States();
	
	/**
	 * 
	 * @param parent
	 * @param maxSize -1 for no maximum
	 * @param expiresTimeout -1 for never expiring
	 * 
	 */
	public DiskCache(
	    final File parent,
	    final long maxSize,
	    final long expiresTimeout
	) {
	    
	    this.parent = parent;
	    this.expiresTimeout = expiresTimeout;
	    
	    if(!parent.exists()) {
	    	parent.mkdirs();
	    }
	    
	    if(parent.exists()) {
	        if(!parent.isDirectory()) {
	            throw new IllegalArgumentException("Cache location already exists and it is not a directory");
	        }
	    }
	    else {
	    	throw new IllegalArgumentException("Cache location already exists and it is not a directory");
	    }
	    
	    for(File child : parent.listFiles()) {
	        
	        states.setCurrentSize(states.getCurrentSize() + child.length());
	        sortedFiles.add(child);
	        
	    }

	    if(maxSize >= 0) {
	        
    	    final Thread daemon = 
    	        new Thread() {
    	        
        	        public void run() {
        	            
        	            try {
        	                
        	                while(true) {
        	                        	                    
        	                    while(states.getCurrentSize() > maxSize) {
        	                        
        	                        File last = null;
        	                        
        	                        try {
        	                            
            	                        synchronized(sortedFiles) {
            	                            
            	                            if(sortedFiles.size() == 0)  {
            	                                break;
            	                            }
            	                                
            	                            last = sortedFiles.last();
            	                            sortedFiles.remove(last);
            	                            
            	                            final long length = last.length();
            	                            
            	                            states.removeFromSize(length);
            	                            
            	                            if(last != null) {
            	                                fileLocks.getLock(last.getAbsolutePath());
            	                            }
            	                            
            	                        }
            	                        
        	                            last.delete();
        	                            
        	                        }
        	                        finally {
        	                            
        	                            if(last != null) {
        	                                fileLocks.releaseLock(last.getAbsolutePath());
        	                            }
        	                        }
    	                            
        	                    }
        	                    
        	                    states.blockTillAdded();
        	                    
        	                }
        	                
        	            }
        	            catch(Exception e) {
        	               logger.error("Error in cleaner daemon for map at root " + parent.getPath(), e);
        	            }
        	            
        	        }
        	        
        	    };
    	    
    	    daemon.setDaemon(true);
    	    daemon.setPriority(Thread.MIN_PRIORITY);
    	    daemon.start();
    	    
	    }
	    
	}
	
	
	@Override
	public void clear() {
	    
	    synchronized(sortedFiles) {
	        
	        states.setCurrentSize(0);
	        
	        for(File file : sortedFiles) {
	            
	            final String path = file.getAbsolutePath(); 
	            
	            try {
	                
    	            try {
                        fileLocks.getLock(path);
                    } 
    	            catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
	            
	                file.delete();
	                
	            }
	            finally {
	                fileLocks.releaseLock(path);
	            }
	            
	        }
	        
	        sortedFiles.clear();
	        
	    }
	    
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	private String getFileNameForKey(final TKey key) {
	    return String.valueOf(key);
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	private File buildCachedImagePath(final String key) {
	    
	    if (parent == null) {
	        return (null);
	    }
	
	    return (buildCachedImagePath(parent, key));
	    
	}
	
	/**
	 * 
	 * @param cacheRoot
	 * @param key
	 * @return
	 */
	private static final File buildCachedImagePath(final File cacheRoot, final String key) {
	    return (new File(cacheRoot, key));
	}
	
	@Override
	public byte [] get(TKey key) throws ResourceException {
	    
	    final File inCache = buildCachedImagePath(getFileNameForKey(key));
	    
	    final String path = inCache.getAbsolutePath();
	    
	    try {
	        
    	    try {
                fileLocks.getLock(path);
            } 
    	    catch (InterruptedException e1) {
               throw new ResourceException(e1);
            }
	        
	        final byte [] returnVal; 
	        
	        
	        final long age = System.currentTimeMillis() - inCache.lastModified();
	        
	        // If the file exists load it then convert it to a <TValue> 
	        if(inCache.exists() && (expiresTimeout <= 0 || age < expiresTimeout )) {
	            
	            final byte [] returnBytes;                
	            final int length;
	            
	            //Check to make sure the length isn't too big to store in a byte []. If it is truncate
	            if(inCache.length() > Integer.MAX_VALUE) {
	                
	                logger.error("BIG PROBLEM FILE IS HUGE SHOULDN'T EVER BE THIS BIG");
	                throw new IllegalArgumentException("File too big");//TODO handle this better
	                
	            }
	            else {
	                length = (int)inCache.length();
	            }
	            
	            returnBytes = new byte[length]; //buffers each file in ram before writing. Probably a better way to do this.
	            
	            //Reader from file
	            try {
	                    
                    if(inCache.exists()) {
                        
                        final InputStream in = new FileInputStream(inCache);
                        inCache.setLastModified(System.currentTimeMillis());
                        
                        synchronized(sortedFiles) {
                            sortedFiles.add(inCache);
                        }
                        
                        try {
                        
                            int read = 0;
                            
                            while(true) {
                                
                                read += in.read(returnBytes, read, length);
                                
                                if(read >= length) {
                                    break;
                                }
                                
                            }    
                            
                        }
                        finally {
                            in.close();
                        }
                        
                    }
	                
	            } 
	            catch (FileNotFoundException e) {
	                
	                e.printStackTrace();
	                return null;
	                
	            }
	            
	            returnVal = returnBytes;
	            
	        }
	        else if(expiresTimeout >= 0 && age > expiresTimeout) {
	            
                states.removeFromSize(inCache.length());
	            
	            synchronized(sortedFiles) {
	                sortedFiles.remove(inCache);
	            }
	            
	            inCache.delete();
	            
	            returnVal = null;
	            
	        }
	        else {
	            returnVal = null;
	        }
	        
	        return returnVal;
	        
	    }
	    catch(IOException e) {
	        
	        logger.error("IO Error just ignore this", e);
	        return null;
	        
	    }
	    finally {
	        fileLocks.releaseLock(path);
	    }
	    
	}
	
	@Override
	public void put(TKey key, byte [] value) {
	    
	    final File inCache = buildCachedImagePath(getFileNameForKey(key));
	    
	    final String path = inCache.getAbsolutePath();
	    
	    try {
    	    
	        try {
                fileLocks.getLock(path);
            } 
            catch (InterruptedException e1) {
               throw new RuntimeException(e1);
            }
	        
	        if(inCache.exists()) {
	            inCache.delete();
	        }
	        
	        final FileOutputStream out = new FileOutputStream(inCache, false);
            final byte [] bites = value;
	        
	        try {
	            
	            out.write(bites);
	            out.flush();
	            
	        }
	        finally {
	            out.close();
	        }    
	        
            states.addSize(bites.length);
	        
	        synchronized(sortedFiles) {
	            sortedFiles.add(inCache);
	        }
	        
	        states.added();
	        
	    }
	    catch (IOException e) {
	        logger.error("Fatal error writting to cache.", e);
	    }
	    finally {
	        fileLocks.releaseLock(path);
	    }
	    
	}
	
	@Override
	public boolean exists(TKey key) {
	    return buildCachedImagePath(getFileNameForKey(key)).exists();
	}
	
	@Override
	public void remove(TKey key) {
		
	    final File file = buildCachedImagePath(getFileNameForKey(key));
	    
	    final String path = file.getAbsolutePath(); 
	    
	    try {
	        
    	    try {
                fileLocks.getLock(path);            
            } 
    	    catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
	        
	        if(file.exists()) {
                file.delete();
            }
	        
	    }
	    finally {
	        fileLocks.releaseLock(path);
	    }
	    
	}

	@Override
	public List<byte[]> getAll(List<TKey> keys) throws ResourceException {
		
		final List<byte []> returnVals = new ArrayList<byte []>();
		
		for(TKey key : keys) {
			returnVals.add(get(key));
		}
		
		return returnVals;
		
	}
	
	private static final class States {
	    
	    private long currentSize;
	    private boolean hasAdded;
	    
	    private final Object cleanerWatch = new Object();
	    
	    public States() {
	        
	    }

        public synchronized void addSize(int length) {
            this.currentSize += length;
        }

        public synchronized long getCurrentSize() {
            return currentSize;
        }

        public synchronized void setCurrentSize(long currentSize) {
            this.currentSize = currentSize;
        }
        
        public synchronized void removeFromSize(long remove) {
            this.currentSize -= remove;
        }
        
        public void added() {
            
            synchronized(cleanerWatch) {
                
                this.hasAdded = true;
                cleanerWatch.notify();
                
            }
            
        }
        
        public void blockTillAdded() {
            
            synchronized(cleanerWatch) {     
                
                if(!this.hasAdded) { //handshake variable in synchronized block so we don't have to put a timeout in wait.
                    
                    try {
                        cleanerWatch.wait();
                    } 
                    catch (InterruptedException e) {
                        logger.error("INTERUPTED:", e);
                    }                    
                    
                }
                
                this.hasAdded = false;
                
            }
            
        }
	    
	}
	
}
