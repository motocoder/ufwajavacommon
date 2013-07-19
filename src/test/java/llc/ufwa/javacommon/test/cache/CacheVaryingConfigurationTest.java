package llc.ufwa.javacommon.test.cache;

import java.io.File;
import java.io.InputStream;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.StringSizeConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.FileHashCache;
import llc.ufwa.data.resource.cache.FilePersistedExpiringCache;
import llc.ufwa.data.resource.cache.FilePersistedMaxSizeCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheVaryingConfigurationTest {
	
	private static final Logger logger = LoggerFactory.getLogger(FilePersistedMaxSizeCacheTest.class);

	private static final byte [] TEN_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	
	static {
	    BasicConfigurator.configure();
	}
	
	@Test
	public void testStackedMaxSizeInExpiringCaches() {
		
		final int maxSize = 180;
		final int expireTimeout = 2000;
		
		final File cacheRoot = new File("./target/test-files/temp2/");
		
		deleteRoot(cacheRoot);
		
		final File dataFolder = new File(cacheRoot, "data");
        final File tempFolder = new File(cacheRoot, "temp");
        
        final File expiringRoot = new File(cacheRoot, "expiringRoot");
        
        final File expiringDataFolder = new File(expiringRoot, "data");
        final File expiringTempFolder = new File(expiringRoot, "temp");
        
        FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        ValueConvertingCache<String, String, byte[]> fileCache = 
            new ValueConvertingCache<String, String, byte []>(
                new ValueConvertingCache<String, byte [], InputStream>(
                        diskCache,
                        new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                    ),
                    new SerializingConverter<String>()
                );
            
        FileHashCache expringPersistDiskCache = new FileHashCache(expiringDataFolder, expiringTempFolder);
        
        Converter<Integer, String> converter = new ReverseConverter<Integer, String>(new StringSizeConverter());
		
        Cache<String, String> cache = 
            new FilePersistedExpiringCache<String>(                    
                new FilePersistedMaxSizeCache<String>(
                    dataFolder,
                    fileCache,
                    converter,
                    maxSize
                ),
                expringPersistDiskCache,
                (long)expireTimeout,
                (long)(expireTimeout * 2)
            );
        
        try {
        	
        	final String TEN_BYTES_STRING = new String(TEN_BYTES);
			
			cache.put("1", TEN_BYTES_STRING);

			Thread.sleep(100);

			cache.put("2", TEN_BYTES_STRING);

			TestCase.assertNotNull(cache.get("1"));
			TestCase.assertNotNull(cache.get("2"));
			
			cache.put("3", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("4", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("5", TEN_BYTES_STRING);

			TestCase.assertNull(cache.get("1"));
			TestCase.assertNull(cache.get("2"));
			TestCase.assertNull(cache.get("3"));
			TestCase.assertNotNull(cache.get("4"));
			TestCase.assertNotNull(cache.get("5"));
			
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
	            
	        expringPersistDiskCache = new FileHashCache(expiringDataFolder, expiringTempFolder);
	        
	        converter = new ReverseConverter<Integer, String>(new StringSizeConverter());
			
	        cache = 
	            new FilePersistedExpiringCache<String>(                    
	                new FilePersistedMaxSizeCache<String>(
	                    dataFolder,
	                    fileCache,
	                    converter,
	                    maxSize
	                ),
	                expringPersistDiskCache,
	                (long)expireTimeout,
	                (long)(expireTimeout * 2)
	            );
			
			TestCase.assertTrue(cache.exists("4"));
			TestCase.assertNotNull(cache.get("5"));
			
			cache.put("6", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("7", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("8", TEN_BYTES_STRING);

		}
		catch (InterruptedException e1) {
			TestCase.fail();
		} 
		catch (ResourceException e) {
		    TestCase.fail();
		}
        
        try {
			
			TestCase.assertFalse(cache.exists("4"));
			TestCase.assertNull(cache.get("5"));
			TestCase.assertNull(cache.get("6"));
			TestCase.assertNotNull(cache.get("7"));
			TestCase.assertNotNull(cache.get("8"));
			
			Thread.sleep(expireTimeout);

			TestCase.assertNull(cache.get("8"));
			
			cache.clear();
			

		} 
		catch (ResourceException e) {

			TestCase.fail("cant get here");
			e.printStackTrace();

		} 
		catch (InterruptedException e) {
			
			TestCase.fail("cant get here");
			e.printStackTrace();
			
		}
		
		cache = null;
		
		deleteRoot(cacheRoot);
		
	}
	
	@Test
	public void testStackedExpiringInMaxSizeCaches() {
		
		final int maxSize = 180;
		final int expireTimeout = 2000;
		
		final File cacheRoot = new File("./target/test-files/temp3/");
		
		deleteRoot(cacheRoot);
		
		final File dataFolder = new File(cacheRoot, "data");
        final File tempFolder = new File(cacheRoot, "temp");
        
        final File expiringRoot = new File(cacheRoot, "expiringRoot");
        
        final File expiringDataFolder = new File(expiringRoot, "data");
        final File expiringTempFolder = new File(expiringRoot, "temp");
        
        FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
        
        ValueConvertingCache<String, String, byte[]> fileCache = 
            new ValueConvertingCache<String, String, byte []>(
                new ValueConvertingCache<String, byte [], InputStream>(
                        diskCache,
                        new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                    ),
                    new SerializingConverter<String>()
                );
            
        FileHashCache expringPersistDiskCache = new FileHashCache(expiringDataFolder, expiringTempFolder);
        
        Converter<Integer, String> converter = new ReverseConverter<Integer, String>(new StringSizeConverter());
		
        Cache<String, String> cache = 
        		new FilePersistedMaxSizeCache<String>(
                        dataFolder,
                        new FilePersistedExpiringCache<String>(                    
                        		fileCache,
                                expringPersistDiskCache,
                                (long)expireTimeout,
                                (long)(expireTimeout * 2)
                            ),
                        converter,
                        maxSize
                    );
        
        try {
        	
        	final String TEN_BYTES_STRING = new String(TEN_BYTES);
			
			cache.put("1", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("2", TEN_BYTES_STRING);

			TestCase.assertNotNull(cache.get("1"));
			TestCase.assertNotNull(cache.get("2"));
			
			cache.put("3", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("4", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("5", TEN_BYTES_STRING);

			TestCase.assertNull(cache.get("1"));
			TestCase.assertNull(cache.get("2"));
			TestCase.assertNull(cache.get("3"));
			TestCase.assertNotNull(cache.get("4"));
			TestCase.assertNotNull(cache.get("5"));
			
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
	            
	        expringPersistDiskCache = new FileHashCache(expiringDataFolder, expiringTempFolder);
	        
	        converter = new ReverseConverter<Integer, String>(new StringSizeConverter());
			
	        cache = 
	            new FilePersistedExpiringCache<String>(                    
	                new FilePersistedMaxSizeCache<String>(
	                    dataFolder,
	                    fileCache,
	                    converter,
	                    maxSize
	                ),
	                expringPersistDiskCache,
	                (long)expireTimeout,
	                (long)(expireTimeout * 2)
	            );
			
			TestCase.assertTrue(cache.exists("4"));
			TestCase.assertNotNull(cache.get("5"));
			
			cache.put("6", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("7", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("8", TEN_BYTES_STRING);

		}
		catch (InterruptedException e1) {
			TestCase.fail();
		} 
		catch (ResourceException e) {
		    TestCase.fail();
		}

		try {
			
			TestCase.assertFalse(cache.exists("4"));
			TestCase.assertNull(cache.get("5"));
			TestCase.assertNull(cache.get("6"));
			TestCase.assertNotNull(cache.get("7"));
			TestCase.assertNotNull(cache.get("8"));
			
			Thread.sleep(expireTimeout);

			TestCase.assertNull(cache.get("8"));
			
			cache.clear();
			
		} 
		catch (ResourceException e) {

			TestCase.fail("cant get here");
			e.printStackTrace();

		} 
		catch (InterruptedException e) {
			
			TestCase.fail("cant get here");
			e.printStackTrace();
			
		}
		
		cache = null;
		
		deleteRoot(cacheRoot);
		
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
