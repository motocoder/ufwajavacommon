package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import llc.ufwa.connection.stream.WrappingInputStream;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.util.StreamUtil;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File cache. Uses temp files when reading from the cache.
 * 
 * Performs cleanup in the same thread as one used so it could block
 * for several seconds depending on use case.
 * 
 * @author swagner
 *
 */
public final class NewDiskCache implements Cache<String, InputStream> {
	
	private final TreeSet<File> sortedFiles = new TreeSet<File>(
        
	    new Comparator<File>() {

            @Override
            public int compare(File item1, File item2) {
                return (int)(item2.lastModified() - item1.lastModified());                
            }
        
        }
        
    );
	
	private final File parent;    
	private final long expiresTimeout;
	private final States states = new States();
    private final long maxSize;
	
	/**
	 * 
	 * @param parent
	 * @param maxSize -1 for no maximum
	 * @param expiresTimeout -1 for never expiring
	 * 
	 */
	public NewDiskCache(
	    final File parent,
	    final long maxSize,
	    final long expiresTimeout
	) {
	    
	    this.parent = parent;
	    this.expiresTimeout = expiresTimeout;
	    this.maxSize = maxSize;
	    
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
	    
	}
	
	private void clean() {
	    
	    System.out.println("Cleaning: " + states.getCurrentSize());
	    
	    if(maxSize >= 0) {            
                                            
            while(states.getCurrentSize() > maxSize) {
                
                System.out.println("currentSize: " + states.getCurrentSize());
            
                if(sortedFiles.size() == 0)  {
                    throw new RuntimeException("Cache thinks it is bigger than max size but contains zero files.");
                }
                    
                final File last = sortedFiles.last();
                
                System.out.println("Removing: " + last.getName());
                
                sortedFiles.remove(last);
                
                final long length = last.length();
                
                states.removeFromSize(length);
                
                last.delete();
                 
            }
            
	    }
	    
	}
	
	@Override
	public void clear() {
	        
        states.setCurrentSize(0);
        
        for(File file : sortedFiles) {              
            file.delete();            
        }
        
        sortedFiles.clear();
	     
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
	public InputStream get(String key) throws ResourceException {
	    
	    clean();
	    
	    final File inCache = buildCachedImagePath(parent, key);
	    
	    final InputStream returnVal;
	    
	    try {
	        
	        final long age = System.currentTimeMillis() - inCache.lastModified();
	        
	        // If the file exists load it then convert it to a <TValue> 
	        if(inCache.exists() && (expiresTimeout <= 0 || age < expiresTimeout )) {
	              
	            //Reader from file
	            try {
	                    
                    if(inCache.exists()) {
                        
                        final InputStream in = new FileInputStream(inCache);
                        
                        try {
                        
                            inCache.setLastModified(System.currentTimeMillis());
                            
                            sortedFiles.add(inCache);
                            
                            final File tempFile = buildCachedImagePath(parent, key + "TeMPoRaRy_FILE");
                        
                            final OutputStream tempOut= new FileOutputStream(tempFile);
                            
                            StreamUtil.copyTo(in, tempOut);
                            
                            tempOut.flush();
                            tempOut.close();
                            
                            final InputStream tempIn = new FileInputStream(tempFile);
                            
                            returnVal = new WrappingInputStream(tempIn) {

                                @Override
                                public void close() throws IOException {
                                    super.close();
                                    
                                    tempFile.delete();
                                    
                                }
                                  
                            };
                            
                            
                        }
                        finally {
                            in.close();
                        }
                        
                    }
                    else {
                        returnVal = null;
                    }
	                
	            } 
	            catch(FileNotFoundException e) {
	                throw new RuntimeException("This cannot happen");	                
	            }
	            
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
	        throw new RuntimeException("This shouldn't happen", e);    
	    }
	    
	}
	
	@Override
	public void put(String key, InputStream in) {
	    
	    clean();
	    
	    final File inCache = buildCachedImagePath(parent, key);
	    	    
	    try {
    	    
	        if(inCache.exists()) {
	            
	            states.removeFromSize(inCache.length());
	            inCache.delete();
	            
	        }
	        
	        final FileOutputStream out = new FileOutputStream(inCache, false);
	        
	        try {
	            
	            StreamUtil.copyTo(in, out);
	            
	            out.flush();	            
	            
	        }
	        finally {
	            out.close();
	        }    
	        
	        final File wroteFile = buildCachedImagePath(parent, key);
	        
            states.addSize(wroteFile.length());
            sortedFiles.add(wroteFile);
	        
	    }
	    catch (IOException e) {
	        throw new RuntimeException("This shouldn't happen", e);
	    }
	    
	}
	
	@Override
	public boolean exists(String key) {
	    
	    clean();
	    
	    return buildCachedImagePath(parent, key).exists();
	    
	}
	
	@Override
	public void remove(String key) {
		
	    clean();
	    
	    final File file = buildCachedImagePath(parent, key);
	    
        if(file.exists()) {
            file.delete();
        }
	     
	}

	@Override
	public List<InputStream> getAll(List<String> keys) throws ResourceException {
		
	    clean();
	    
		final List<InputStream> returnVals = new ArrayList<InputStream>();
		
		for(String key : keys) {
			returnVals.add(get(key));
		}
		
		return returnVals;
		
	}
	
	private static final class States {
	    
	    private long currentSize;
	    
	    public States() {
	        
	    }

        public void addSize(long length) {
            this.currentSize += length;
        }

        public long getCurrentSize() {
            return currentSize;
        }

        public void setCurrentSize(long currentSize) {
            this.currentSize = currentSize;
        }
        
        public void removeFromSize(long remove) {
            this.currentSize -= remove;
        }
        
	}
	
}
