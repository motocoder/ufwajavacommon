package llc.ufwa.javacommon.test.cache;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.FileCache;
import llc.ufwa.data.resource.cache.FilePersistedExpiringCache;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilePersistedExpiringCacheTest {

    private static final Logger logger = LoggerFactory.getLogger(FilePersistedExpiringCacheTest.class);

    @Test
    public void testExpiringPart() {
    	
    	try {
    		
	    	final long timeout = 1000;
			
	        final File dataFolder = new File("./target/test-files/temp");
	        final File tempFolder = new File("./target/test-files/temp/data");
	        
	        deleteRoot(tempFolder);
	
	        final File internalFile = new File(tempFolder, "ad-properties"); 
	        final File rootFile = new File(dataFolder, "ad-properties");
	        
			Cache<String, InputStream> internal = new FileCache(internalFile, -1L, timeout);
			Cache<String, InputStream> InpersistingRoot = new FileCache(rootFile, -1L, timeout);
	
			final long cleanupTimeout = 1500;
			
			final Cache<String, InputStream> cache = new FilePersistedExpiringCache(internal, InpersistingRoot, timeout, cleanupTimeout);
			
			final String key = "dfsa";
	        final String value = "dfsadsf";
	        final String key2 = "fgdd";
	        final String value2 = "dfgsds";
	        String returnValue;
	        
	        // TEST PUT, GET, REMOVE, and EXISTS
	        
	        cache.put(key, new ByteArrayInputStream(value.getBytes()));
	        
	        InputStream baos = cache.get(key);
			
	        returnValue = getStringFromInputStream(baos);
	        
	        TestCase.assertEquals(value, returnValue);
	        TestCase.assertEquals(cache.exists(key), true);

	        Thread.sleep(timeout+100);
	        
	        TestCase.assertEquals(cache.exists(key), false); //this should be false because it's after the timeout time
			
		}
		catch (ResourceException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    }
    	
	public void universalTest() {

		try {
			
			final long timeout = 1000;
			
			Random random = new Random();
			int suffix = random.nextInt(4000);
			
	        final File dataFolder = new File("./target/test-files/temp");
	        final File tempFolder = new File("./target/test-files/temp/data" + String.valueOf(suffix)); //append random number onto the end of the folder so multithreading works
	        
	        deleteRoot(tempFolder);
	
	        final File internalFile = new File(tempFolder, "ad-properties"); 
	        final File rootFile = new File(dataFolder, "ad-properties");
	        
			Cache<String, InputStream> internal = new FileCache(internalFile, -1L, timeout);
			Cache<String, InputStream> InpersistingRoot = new FileCache(rootFile, -1L, timeout);
	
			final long cleanupTimeout = 1500;
			
			final Cache<String, InputStream> cache = new FilePersistedExpiringCache(internal, InpersistingRoot, timeout, cleanupTimeout);
			
			final String key = "dfsa";
	        final String value = "dfsadsf";
	        final String key2 = "fgdd";
	        final String value2 = "dfgsds";
	        String returnValue;
	        
	        // TEST PUT, GET, REMOVE, and EXISTS
	        
	        cache.put(key, new ByteArrayInputStream(value.getBytes()));
	        
	        InputStream baos = cache.get(key);
			
	        returnValue = getStringFromInputStream(baos);
	        
	        TestCase.assertEquals(value, returnValue);
	        TestCase.assertEquals(cache.exists(key), true);
	        
	        cache.remove(key);
	        
	        TestCase.assertEquals(cache.exists(key), false);
	        
	        // TEST CLEAR, GETALL, and RETEST EXISTS
	        
	        List<String> keyList = new ArrayList<String>();
	        
	        keyList.add(key);
	        keyList.add(key2);
	        
	        cache.put(key, new ByteArrayInputStream(value.getBytes()));
	        cache.put(key2, new ByteArrayInputStream(value2.getBytes()));
	
	        TestCase.assertEquals(cache.exists(key), true);
	        TestCase.assertEquals(cache.exists(key2), true);
	        
	        List<InputStream> streamList = cache.getAll(keyList);
	        
	        for (int y = 0; y < 1; y++) {
		        
	        	InputStream baosStream = streamList.get(y);
				
		        returnValue = getStringFromInputStream(baosStream);
		        
		        TestCase.assertEquals(value, returnValue);
		        TestCase.assertEquals(cache.exists(key), true);
		        
	        }
	        
	        cache.clear();
		       
	        TestCase.assertEquals(cache.exists(key), false);
	        TestCase.assertEquals(cache.exists(key2), false);
        
		}
		catch (ResourceException e) {
			e.printStackTrace();
		}
		
	}
	
	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {
	    
	    if(is == null) {
	        throw new NullPointerException("InputStream is null");
	    }
 
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
 
		String line;
		try {
 
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
 
		return sb.toString();
 
	}

	void deleteRoot (File root) {
        if (root.exists()) {
            
            if(root.listFiles() != null) {
                for (File cacheFile: root.listFiles()){
                    cacheFile.delete();
                }
            }
            root.delete();
        }
    }
	
}
