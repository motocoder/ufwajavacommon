package llc.ufwa.javacommon.test.cache;

import java.io.File;
import java.io.InputStream;
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
import llc.ufwa.data.resource.cache.KeyEncodingCache;
import llc.ufwa.data.resource.cache.MemoryCache;
import llc.ufwa.data.resource.cache.SynchronizedCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilePersistedMaxSizeCacheTest {

	private static final Logger logger = LoggerFactory.getLogger(FilePersistedMaxSizeCacheTest.class);

	private static final byte [] TEN_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	
	static {
	    BasicConfigurator.configure();
	}

	@Test
	public void testPersistedPart() {
		
		Random random = new Random();
		String appendix = String.valueOf(Math.abs(random.nextInt()));
		
		File root2 = new File("./target/test-files/temp" + appendix + "/");
		
		deleteRoot(root2);
		
		final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(new StringSizeConverter());
		
		final File dataFolder = new File(root2, "data");
        final File tempFolder = new File(root2, "temp");
        final File persistingFolder = new File(root2, "persisting");
        
		FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
		
		Cache<String, String> fileCache = 
			new ValueConvertingCache<String, String, byte []>(
				new ValueConvertingCache<String, byte [], InputStream>(
						diskCache,
						new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
					),
					new SerializingConverter<String>()
				);
			
		Cache<String, String> cache =
            new FilePersistedMaxSizeCache<String>(
                persistingFolder,
                fileCache,
                converter,
                150
            );

		try {
			
			cache.clear();
			
			final String TEN_BYTES_STRING = new String(TEN_BYTES);
			
			cache.put("1", TEN_BYTES_STRING);

			Thread.sleep(100);

			cache.put("2", TEN_BYTES_STRING);

			TestCase.assertNotNull(cache.get("1"));
			TestCase.assertNotNull(cache.get("2"));
			
			cache = null;
			fileCache = null;
			diskCache = null;
			
			Thread.sleep(500);
			
			TestCase.assertNull(cache);

			diskCache = new FileHashCache(dataFolder, tempFolder);
			
			fileCache = 
				new ValueConvertingCache<String, String, byte []>(
					new ValueConvertingCache<String, byte [], InputStream>(
							diskCache,
							new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
						),
						new SerializingConverter<String>()
					);
				
			cache =
	            new FilePersistedMaxSizeCache<String>(
	                persistingFolder,
	                fileCache,
	                converter,
	                125
	            );
			
			TestCase.assertTrue(cache.exists("1"));
			TestCase.assertNotNull(cache.get("2"));
			
			cache.put("3", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("4", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("5", TEN_BYTES_STRING);

		}
		catch (InterruptedException e1) {
			TestCase.fail();
		} 
		catch (ResourceException e) {
		    TestCase.fail();
		}

		try {
			
			TestCase.assertFalse(cache.exists("1"));
			TestCase.assertNull(cache.get("2"));
			TestCase.assertNotNull(cache.get("3"));
			TestCase.assertNotNull(cache.get("4"));
			TestCase.assertNotNull(cache.get("5"));

		} 
		catch (ResourceException e) {

			TestCase.fail("cant get here");
			e.printStackTrace();

		}
		
		deleteRoot(root2);
		
	}
	
//	@Test
//	public void testMaxSizePart() {
//		
//		Random random = new Random();
//		String appendix = String.valueOf(Math.abs(random.nextInt()));
//		
//		File root2 = new File("./target/test-files/temp" + appendix + "/");
//		
//		deleteRoot(root2);
//		
//		final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(new StringSizeConverter());
//		
//		final File dataFolder = new File(root2, "data");
//        final File tempFolder = new File(root2, "temp");
//        final File persistingFolder = new File(root2, "persisting");
//        
//		final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
//		
//		final Cache<String, String> fileCache = 
//			new ValueConvertingCache<String, String, byte []>(
//				new ValueConvertingCache<String, byte [], InputStream>(
//						diskCache,
//						new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
//					),
//					new SerializingConverter<String>()
//				);
//			
//		final Cache<String, String> cache =
//            new FilePersistedMaxSizeCache<String>(
//                persistingFolder,
//                fileCache,
//                converter,
//                150
//            );
//
//		try {
//			
//			cache.clear();
//			
//			final String TEN_BYTES_STRING = new String(TEN_BYTES);
//			
//			cache.put("1", TEN_BYTES_STRING);
//
//			Thread.sleep(100);
//
//			cache.put("2", TEN_BYTES_STRING);
//
//			Thread.sleep(50);
//
//			cache.put("3", TEN_BYTES_STRING);
//
//			Thread.sleep(50);
//
//			cache.put("4", TEN_BYTES_STRING);
//
//			Thread.sleep(50);
//
//			cache.put("5", TEN_BYTES_STRING);
//
//		}
//		catch (InterruptedException e1) {
//			e1.printStackTrace();
//		} 
//		catch (ResourceException e) {
//			e.printStackTrace();
//		}
//
//		try {
//			
//			TestCase.assertFalse(cache.exists("1"));
//			TestCase.assertNull(cache.get("2"));
//			TestCase.assertNull(cache.get("3"));
//			TestCase.assertNotNull(cache.get("4"));
//			TestCase.assertNotNull(cache.get("5"));
//
//		} 
//		catch (ResourceException e) {
//
//			TestCase.fail("cant get here");
//			e.printStackTrace();
//
//		}
//		
//		deleteRoot(root2);
//		
//	}
//	
//	
//	@Test 
//    public void testFilePersistedExpiringCacheTest() {
//		
//		try {
//
//			Random random = new Random();
//			String appendix = String.valueOf(Math.abs(random.nextInt()));
//			
//			File root = new File("./target/test-files/temp" + appendix + "/");
//			
//			deleteRoot(root);
//			
//			final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(new StringSizeConverter());
//			
//			final File dataFolder = new File(root, "data");
//	        final File tempFolder = new File(root, "temp");
//	        final File persistingFolder = new File(root, "persisting");
//	        
//			final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
//			
//			final Cache<String, String> fileCache = 
//				new ValueConvertingCache<String, String, byte []>(
//					new ValueConvertingCache<String, byte [], InputStream>(
//							diskCache,
//							new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
//						),
//						new SerializingConverter<String>()
//					);
//				
//			final Cache<String, String> cache =
//                new FilePersistedMaxSizeCache<String>(
//                    persistingFolder,
//                    fileCache,
//                    converter,
//                    1000
//                );
//			
//			
//			final String key = "dfsa";
//	        final String value = "dfsadsf";
//	        final String key2 = "fgdd";
//	        final String value2 = "dfgsds";
//	        String returnValue;
//	        
//	        // TEST PUT, GET, REMOVE, and EXISTS
//	        
//	        cache.put(key, value);
//	        
//	        returnValue = cache.get(key);
//	        
//	        TestCase.assertEquals(value, returnValue);
//	        TestCase.assertEquals(cache.exists(key), true);
//	        
//	        cache.remove(key);
//	        
//	        TestCase.assertEquals(cache.exists(key), false);
//	        
//	        // TEST CLEAR, GETALL, and RETEST EXISTS AND GET
//	        
//	        List<String> keyList = new ArrayList<String>();
//	        
//	        keyList.add(key);
//	        keyList.add(key2);
//	        
//	        cache.put(key, value);
//	        cache.put(key2, value2);
//	
//	        TestCase.assertEquals(cache.exists(key), true);
//	        TestCase.assertEquals(cache.exists(key2), true);
//	        
//	        List<String> stringList = cache.getAll(keyList);
//	        
//	        for (int y = 0; y < 1; y++) {
//		        
//	        	returnValue = stringList.get(y);
//		        
//		        TestCase.assertEquals(value, returnValue);
//		        TestCase.assertEquals(cache.exists(key), true);
//		        
//	        }
//	        
//	        cache.clear();
//		       
//	        TestCase.assertEquals(cache.exists(key), false);
//	        TestCase.assertEquals(cache.get(key), null);
//			
//			deleteRoot(root);
//        
//		}
//		catch (ResourceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	
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
	
}
