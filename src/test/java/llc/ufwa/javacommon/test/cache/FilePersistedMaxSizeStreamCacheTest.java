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
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.IntegerStringConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.StringSizeConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.ExpiringCache;
import llc.ufwa.data.resource.cache.FileCache;
import llc.ufwa.data.resource.cache.FileHashCache;
import llc.ufwa.data.resource.cache.FilePersistedMaxSizeCache;
import llc.ufwa.data.resource.cache.FilePersistedMaxSizeStreamCache;
import llc.ufwa.data.resource.cache.KeyEncodingCache;
import llc.ufwa.data.resource.cache.MemoryCache;
import llc.ufwa.data.resource.cache.SynchronizedCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilePersistedMaxSizeStreamCacheTest {

	private static final Logger logger = LoggerFactory.getLogger(FilePersistedMaxSizeStreamCacheTest.class);

	private static final byte [] TEN_BYTES = new byte[10];
	
	static {
	    BasicConfigurator.configure();
	}

	@Test
	public void testPersistedPart() {
		
		Random random = new Random();
		String appendix = String.valueOf(Math.abs(random.nextInt()));
		
		File root2 = new File("./target/test-files/temp" + appendix + "/");
		
		deleteRoot(root2);
		
		final File dataFolder = new File(root2, "data");
        final File tempFolder = new File(root2, "temp");
        final File temp2Folder = new File(root2, "temp2");
        final File persistingFolder = new File(root2, "persisting");

        FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        Cache<String, InputStream> fileCache = 
            new SynchronizedCache<String, InputStream> (
                diskCache
            );
			
		Cache<String, InputStream> cache =
            new FilePersistedMaxSizeStreamCache(
                persistingFolder,
                fileCache,
                20
            );

		try {
			
			cache.clear();
			
			final String TEN_BYTES_STRING = new String(TEN_BYTES);
			
			cache.put("1", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			Thread.sleep(100);

			cache.put("2", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			System.out.println(getStringFromInputStream(cache.get("1")));
			
			TestCase.assertNotNull(getStringFromInputStream(cache.get("1")));
			TestCase.assertNotNull(getStringFromInputStream(cache.get("2")));
			
			cache.put("3", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			Thread.sleep(50);

			cache.put("4", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			Thread.sleep(50);

			cache.put("5", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			TestCase.assertNull(getStringFromInputStream(cache.get("1")));
			TestCase.assertNull(getStringFromInputStream(cache.get("2")));
			TestCase.assertNull(getStringFromInputStream(cache.get("3")));
			TestCase.assertNotNull(getStringFromInputStream(cache.get("4")));
			TestCase.assertNotNull(getStringFromInputStream(cache.get("5")));
			
			cache = null;
			fileCache = null;
			diskCache = null;
			
			Thread.sleep(500);
			
			TestCase.assertNull(cache);

	        final FileHashCache diskCache2 = new FileHashCache(dataFolder, temp2Folder);
	        
	        final Cache<String, InputStream> fileCache2 = 
	            new SynchronizedCache<String, InputStream> (
	                diskCache2
	            );
				
			cache =
	            new FilePersistedMaxSizeStreamCache(
	                persistingFolder,
	                fileCache2,
	                20
	            );
			
			TestCase.assertTrue(cache.exists("4"));
			TestCase.assertNotNull(getStringFromInputStream(cache.get("5")));
			
			cache.put("6", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			Thread.sleep(50);

			cache.put("7", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			Thread.sleep(50);

			cache.put("8", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

		}
		catch (InterruptedException e1) {
			TestCase.fail();
		} 
		catch (ResourceException e) {
		    TestCase.fail();
		}

		try {
			
			TestCase.assertFalse(cache.exists("4"));
			TestCase.assertNull(getStringFromInputStream(cache.get("5")));
			TestCase.assertNull(getStringFromInputStream(cache.get("6")));
			TestCase.assertNotNull(getStringFromInputStream(cache.get("7")));
			TestCase.assertNotNull(getStringFromInputStream(cache.get("8")));

		} 
		catch (ResourceException e) {

			TestCase.fail("cant get here");
			e.printStackTrace();

		}
		
		deleteRoot(root2);
		
	}
	
	@Test
	public void testMaxSizePart() {
		
		Random random = new Random();
		String appendix = String.valueOf(Math.abs(random.nextInt()));
		
		File root2 = new File("./target/test-files/temp" + appendix + "/");
		
		deleteRoot(root2);
		
		final File dataFolder = new File(root2, "data");
        final File tempFolder = new File(root2, "temp");
        final File persistingFolder = new File(root2, "persisting");

        FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        Cache<String, InputStream> fileCache = 
            new SynchronizedCache<String, InputStream> (
                diskCache
            );
			
		Cache<String, InputStream> cache =
            new FilePersistedMaxSizeStreamCache(
                persistingFolder,
                fileCache,
                20
            );

		try {
			
			cache.clear();
			
			final String TEN_BYTES_STRING = new String(TEN_BYTES);
			
			cache.put("1", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			Thread.sleep(100);

			cache.put("2", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			Thread.sleep(50);

			cache.put("3", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			Thread.sleep(50);

			cache.put("4", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

			Thread.sleep(50);

			cache.put("5", new ByteArrayInputStream(TEN_BYTES_STRING.getBytes()));

		}
		catch (InterruptedException e1) {
			e1.printStackTrace();
		} 
		catch (ResourceException e) {
			e.printStackTrace();
		}

		try {
			
			TestCase.assertFalse(cache.exists("1"));
			TestCase.assertNull(cache.get("2"));
			TestCase.assertNull(cache.get("3"));
			TestCase.assertNotNull(cache.get("4"));
			TestCase.assertNotNull(cache.get("5"));

		} 
		catch (ResourceException e) {

			TestCase.fail("cant get here");
			e.printStackTrace();

		}
		
		deleteRoot(root2);
		
	}
	
	
	@Test 
    public void testFilePersistedExpiringCacheTest() {
		
		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));
			
			File root = new File("./target/test-files/temp" + appendix + "/");
			
			deleteRoot(root);
			
			final File dataFolder = new File(root, "data");
	        final File tempFolder = new File(root, "temp");
	        final File persistingFolder = new File(root, "persisting");
	        
	        FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
	        
	        Cache<String, InputStream> fileCache = 
	            new SynchronizedCache<String, InputStream> (
	                diskCache
	            );
				
			Cache<String, InputStream> cache =
	            new FilePersistedMaxSizeStreamCache(
	                persistingFolder,
	                fileCache,
	                150
	            );
			
			
			final String key = "dfsa";
	        final String value = "dfsadsf";
	        final String key2 = "fgdd";
	        final String value2 = "dfgsds";
	        InputStream returnValue;
	        
	        // TEST PUT, GET, REMOVE, and EXISTS
	        
	        cache.put(key, new ByteArrayInputStream(value.getBytes()));
	        
	        returnValue = cache.get(key);
	        
	        TestCase.assertEquals(value, getStringFromInputStream(returnValue));
	        TestCase.assertEquals(cache.exists(key), true);
	        
	        cache.remove(key);
	        
	        TestCase.assertEquals(cache.exists(key), false);
	        
	        // TEST CLEAR, GETALL, and RETEST EXISTS AND GET
	        
	        List<String> keyList = new ArrayList<String>();
	        
	        keyList.add(key);
	        keyList.add(key2);
	        
	        cache.put(key, new ByteArrayInputStream(value.getBytes()));
	        cache.put(key2, new ByteArrayInputStream(value2.getBytes()));
	        cache.put(key2, new ByteArrayInputStream(value2.getBytes())); //repeat to test the automatic remove() when duplicate
	
	        TestCase.assertEquals(cache.exists(key), true);
	        TestCase.assertEquals(cache.exists(key2), true);
	        
	        List<InputStream> stringList = cache.getAll(keyList);
	        
	        for (int y = 0; y < 1; y++) {
		        
	        	returnValue = stringList.get(y);
		        
		        TestCase.assertEquals(value, getStringFromInputStream(returnValue));
		        TestCase.assertEquals(cache.exists(key), true);
		        
	        }
	        
	        cache.clear();
		    
	        TestCase.assertEquals(cache.exists(key), false);
	        TestCase.assertEquals(cache.get(key), null);
			
			deleteRoot(root);
        
		}
		catch (ResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test 
    public void negativeSizeTest() {
		
		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));
			
			File root = new File("./target/test-files/temp" + appendix + "/");
			
			deleteRoot(root);
			
			final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(new StringSizeConverter());
			
			final File dataFolder = new File(root, "data");
	        final File tempFolder = new File(root, "temp");
	        final File persistingFolder = new File(root, "persisting");
	        
	        FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
	        
	        Cache<String, InputStream> fileCache = 
	            new SynchronizedCache<String, InputStream> (
	                diskCache
	            );
				
			Cache<String, InputStream> cache =
	            new FilePersistedMaxSizeStreamCache(
	                persistingFolder,
	                fileCache,
	                150
	            );
		
			String key = "dfsa";
			final String key1 = "dfewfawfsdsfsdfadsadsfvsa";
			final String key2 = "dfsewr34fara";
			final String value2 = "dsfaskdfaskfjhasjkdfhaskfjldhaskfjhaskjfhkashdfkasjhdfkahsdfkljhs";
			final String key4 = "9un98q5n3miodfsa";
	        final String value = "qv54v3ckljhdsfoyh43ods";
	        InputStream returnValue;
	        
	        cache.put(key, new ByteArrayInputStream(value.getBytes()));
	        cache.put(key1, new ByteArrayInputStream(value2.getBytes()));
	        cache.put(key2, new ByteArrayInputStream(value2.getBytes()));
	        cache.put(key4, new ByteArrayInputStream(value.getBytes()));
	        
	        returnValue = cache.get(key4);
	        
	        TestCase.assertEquals(value, getStringFromInputStream(returnValue));
	        TestCase.assertEquals(cache.exists(key4), true);
	        
	        cache.remove(key4);
	        
	        TestCase.assertEquals(cache.exists(key4), false);
	        
	        
			deleteRoot(root);
        
		}
		catch (ResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void deleteRoot (File root) {
	    
		if (root.exists()) {
		    
		    final File[] fileList = root.listFiles();
		    
		    if(fileList != null) {
		        
    			for (File cacheFile : fileList){
    				cacheFile.delete();
    			}
    			
		    }
		    
			root.delete();
			
		}
	}
	
	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {
	    
	    if(is == null) {
	        return null;
	    	//throw new NullPointerException("InputStream is null");
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
		
		String retString = sb.toString();
		
	 	if (retString == "") {
	 		return null;
	 	}
	 	else {
	 		return retString;
	 	}
 
	}
	
}
