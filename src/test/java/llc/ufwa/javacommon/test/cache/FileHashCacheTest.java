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
import llc.ufwa.data.resource.cache.FileHashCache;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileHashCacheTest {

    private static final Logger logger = LoggerFactory.getLogger(FileHashCacheTest.class);
    
    static {
    	BasicConfigurator.configure();
    }
	
	@Test 
    public void testFileHashCache() {
		
		//for (int x = 0; x < 10; x++) {
			universalTest();
		//}
	
	}
	
//	@Test 
//    public void testFileHashCacheMultiThreaded() {
//		
//		for (int x = 0; x < 15; x++) {
//			 new Thread() {
//	                
//	                @Override
//	                public void run() {
//	                	
//            			universalTest();
//	                	
//	                }
//	                
//			 }.start();
//		}
//	
//	}
	
	public void universalTest() {
		
		try {
			
	        final File tempFolder = new File("./target/test-files/temp-data");
	        final File dataFolder = new File("./target/test-files/data");
	        final File dataFolderItem = new File("./target/test-files/data/data");
	        
	        deleteRoot(tempFolder);
	        deleteRoot(dataFolder);
	        dataFolderItem.delete();
	        
	        tempFolder.mkdirs();
	        dataFolder.mkdirs();
	        
	        final FileHashCache cache = new FileHashCache(dataFolderItem, tempFolder);
	        
	        final String key = "dtsffffdfsjhgjnvdfsddfssdffsdfewfeasdf";
	        final String value = "ddwerfsadfwefwaefwfawfewsadfsad4";
	        final String key2 = "dtsffffdfsjhgjnvdfsddfsasdeasdf";
	        final String value2 = "saasdfasdfdfasdfaewasfsadfasfdadscasdfasdfasdfsdfg";
	        final String key3 = "adatiwefwwawfwfdfdsgdfgsdgasddfsds";
	        String returnValue;
	        
	        // TEST PUT, GET, REMOVE, and EXISTS
	        
	        cache.put(key, new ByteArrayInputStream(value.getBytes()));
	        
	        InputStream baos = cache.get(key);
			
	        returnValue = getStringFromInputStream(baos);
	        
	        TestCase.assertEquals(value, returnValue);
	        TestCase.assertEquals(cache.exists(key), true);
	        
	        cache.remove(key);
	        
	        TestCase.assertEquals(cache.exists(key), false);
	        
	        baos.close();
	        
	        // TEST CLEAR, GETALL, and RETEST EXISTS
	        
	        List<String> keyList = new ArrayList<String>();
	        
	        keyList.add(key3);
	        keyList.add(key2);
	        
	        cache.put(key3, new ByteArrayInputStream(value.getBytes()));
	        cache.put(key2, new ByteArrayInputStream(value2.getBytes()));

	        Thread.sleep(500);
	        
	        TestCase.assertEquals(true, cache.exists(key3));
	        TestCase.assertNotNull(cache.get(key2));
	        TestCase.assertEquals(true, cache.exists(key2));
	        
	        List<InputStream> streamList = cache.getAll(keyList);
	        
	        for (int y = 0; y < 1; y++) {
		        
	        	InputStream baosStream = streamList.get(y);
				
		        returnValue = getStringFromInputStream(baosStream);
		        
		        TestCase.assertEquals(value, returnValue);
		        TestCase.assertEquals(cache.exists(key3), true);
		        
	        }
	        
	        cache.clear();
		       
	        TestCase.assertEquals(cache.exists(key3), false);
	        TestCase.assertEquals(cache.exists(key2), false);
	        
		}
		catch (ResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
            
        	final File[] listed = root.listFiles();
        	
        	if(listed != null) {	        	
	            for (File cacheFile: listed){
	                cacheFile.delete();
	            }
        	}
            
            root.delete();
        }
    }

}
