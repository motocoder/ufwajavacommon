package llc.ufwa.javacommon.test.cache;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.StringSizeConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.FileHashCache;
import llc.ufwa.data.resource.cache.FilePersistedMaxSizeCache;
import llc.ufwa.data.resource.cache.SynchronizedCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;
import llc.ufwa.util.StreamUtil;
import llc.ufwa.util.StringUtilities;

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
	public void universalTest() {

        final File tempFolder = new File("/target/test-files/temp-dataa");
        final File dataFolder = new File("/target/test-files/dataa");
        final File dataFolderItem = new File("/target/test-files/data/dataa");
        
		try {
			
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
	        final String key3 = "adatiwefwwawfwfdfdsgdfgsdgasdd982y4d8913y894d012ny4dyn328yd4n289314yndm9812ynd409jkjlafoasudf8904uy04uf4309u0oidsnhfioashd9834jh0q4f08943qhlq34hf89034fh48fih3fsds";
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			deleteRoot(tempFolder);
	        deleteRoot(dataFolder);
	        dataFolderItem.delete();
		}
		
	}
	
	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {
	    
	    try {
	        
    	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    	    
    	    StreamUtil.copyTo(is, out);
    	    
     
    		return new String(out.toByteArray(), "UTF-8");
    		
	    }
	    catch(Exception e) {
	        
	        TestCase.fail();
	        throw new RuntimeException();
	        
	    }
 
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
	
	@Test 
	public void secondaryTest() {
		
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
	        
	        // create very long string by concatenation
	        String key = "abcdefghijklmnopqrstuvwxyz0123456789";
	        String value = "abcdefghijklmnopqrstuvwxyz0123456789";
	        int iterations = 3;
	        
	        
	        for (int i = 0; i < iterations; i++) {
	        	key += key.concat(key);
	        	value += value.concat(value);
	        }
	        
	        logger.info("value is this long: " + value.length());
	        
	        String returnValue;
	        
	        // TEST PUT, GET, REMOVE, and EXISTS
	        
	        for (int i = 0; i < 10; i++) {
	        	cache.put(key, new ByteArrayInputStream(value.getBytes()));
	        }
	        
	        InputStream baos = null;
	        
	        for (int i = 0; i < 10; i++) {
	        	baos = cache.get(key);
	        }
			
	        returnValue = getStringFromInputStream(baos);
	        
	        TestCase.assertEquals(value, returnValue);
	        for (int i = 0; i < 10; i++) {
	        	TestCase.assertEquals(cache.exists(key), true);
	        }
	        
	        for (int i = 0; i < 10; i++) {
	        	cache.clear(); //Test Clear
	        }
	        
	        TestCase.assertEquals(cache.exists(key), false);
	        
	        baos.close();
	        
		}
		catch (ResourceException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test 
	public void multiThreadedTest() {
		
		final ExecutorService pool = Executors.newFixedThreadPool(10);

        final File tempFolder = new File("./target/test-files/temp-data");
        final File dataFolder = new File("./target/test-files/data");
        final File dataFolderItem = new File("./target/test-files/data/data");
        
        deleteRoot(tempFolder);
        deleteRoot(dataFolder);
        dataFolderItem.delete();
        
        tempFolder.mkdirs();
        dataFolder.mkdirs();
        
        final Cache<String, InputStream> fileCache = new FileHashCache(dataFolderItem, tempFolder);
		final Cache<String, InputStream> cache = new SynchronizedCache<String, InputStream>(fileCache);
		
		logger.info("created cache");
		
		for (int x = 0; x < 9; x++) {
			
			pool.execute(
                    
                new Runnable() {

                    @Override
                    public void run() {
					
						try {
					
					        // create varying length strings by concatenation
							final String value = "adasdfasdfasfdasfasdfdfsdf";
							final Random random = new Random();
					        
							final String key = "e" + String.valueOf(random.nextInt(999999));
					        
					        logger.info("putting value: " + value + " with the key of " + key);
					        
					        // TEST PUT, GET, REMOVE, and EXISTS
					        
					        cache.put(key, new ByteArrayInputStream(value.getBytes()));
					        
					        final String returnValue = getStringFromInputStream(cache.get(key));
					        
					        TestCase.assertEquals(value, returnValue);
					        TestCase.assertEquals(cache.exists(key), true);
					        
						} catch (ResourceException e) {
							e.printStackTrace();
						}
			        
                    }
                    
                }
                
			);
			
		}
		
		pool.shutdown();
		
		try {
			pool.awaitTermination(1000, TimeUnit.SECONDS);
		} 
		catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
	}
	
	@Test
	public void extraTests() {
		
		try {
	        
			File root2 = new File("./target/test-files/temp/");
			
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
	        
	        final String key = "dtsffffdfsjhgjnvdfsddfssdffsdfewfeasdf";
	        final String value = "ddwerfsadfwefwaefwfawfewsadfsad4";
	        final String key2 = "dtsffffdfsjhgjnvdfsddfsasdeasdf";
	        final String key3 = "adatiwefwwawfwfdfdsgdfgsdgasdd982y4d8913y894d012ny4dyn328yd4n289314yndm9812ynd409jkjlafoasudf8904uy04uf4309u0oidsnhfioashd9834jh0q4f08943qhlq34hf89034fh48fih3fsds";
	        String returnValue;
	        
	        // TEST PUT, GET, REMOVE, and EXISTS
	        
	        cache.put(key, value);
	        
	        returnValue = cache.get(key);
	        
	        TestCase.assertEquals(value, returnValue);
	        TestCase.assertEquals(cache.exists(key), true);
	        
	        cache.clear();
	        
	        TestCase.assertEquals(cache.exists(key), false);
	        
	        cache.clear();
		       
	        TestCase.assertEquals(cache.exists(key), false);
	        
		}
		catch (ResourceException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test 
	public void varyingKeyLengthTest() {
		
		try {
			
			for (int x = 0; x < 18; x++) {
				
		        final File tempFolder = new File("./target/test-files/temp-data");
		        final File dataFolder = new File("./target/test-files/data");
		        final File dataFolderItem = new File("./target/test-files/data/data");
		        
		        deleteRoot(tempFolder);
		        deleteRoot(dataFolder);
		        dataFolderItem.delete();
		        
		        tempFolder.mkdirs();
		        dataFolder.mkdirs();
		        
		        final Cache<String, InputStream> fileCache = new FileHashCache(dataFolderItem, tempFolder);
				final Cache<String, InputStream> cache = new SynchronizedCache<String, InputStream>(fileCache);
				
		        // create varying length strings by concatenation
		        String key = "abcdef0123456789";
		        String value = "abcdefghi";
		        
		        for (int i = 0; i < x; i++) {
		        	value = value.concat(value);
		        	key = key.concat("f");
		        	
		        	
		        }
		        
		        logger.info("putting value of length: " + value.length() + " with key of length: " + key.length());
		        
		        // TEST PUT, GET, REMOVE, and EXISTS
		        
		        logger.debug("writting value " + value.getBytes().length);
		        
		        cache.put(key, new ByteArrayInputStream(value.getBytes()));
		        
		        String returnValue = getStringFromInputStream(cache.get(key));
			
		        logger.debug(returnValue);
		        logger.debug(value);
		        
		        TestCase.assertEquals(value.length(), returnValue.length());
		        TestCase.assertEquals(value, returnValue);
		        TestCase.assertEquals(cache.exists(key), true);
		        
		        cache.clear(); // test clear
		        
		        TestCase.assertEquals(cache.exists(key), false);
		        
			}
		        
		}
		catch (ResourceException e) {
			e.printStackTrace();
		} 
		finally {
			
		}
		
	}
	
	@Test 
	public void assortedTest() {

		final File root2 = new File("/target/test-files/tempor-dataa");
		
		try {
			
			for (int x = 0; x < 18; x++) {
				
		        final File tempFolder = new File("./target/test-files/temp-data");
		        final File dataFolder = new File("./target/test-files/data");
		        final File dataFolderItem = new File("./target/test-files/data/data");
		        
		        deleteRoot(tempFolder);
		        deleteRoot(dataFolder);
		        dataFolderItem.delete();
		        
		        tempFolder.mkdirs();
		        dataFolder.mkdirs();
		        
		        final Cache<String, InputStream> fileCache = new FileHashCache(dataFolderItem, tempFolder);
				final Cache<String, InputStream> cache = new SynchronizedCache<String, InputStream>(fileCache);
				
		        // create varying length strings by concatenation
		        String key = "abcdef0123456789";
		        String value = "abcdefghi";
		        
		        for (int i = 0; i < x; i++) {
		        	value = value.concat(value);
		        	key = key.concat("f");
		        }
		        
		        logger.info("putting value of length: " + value.length() + " with key of length: " + key.length());
		        
		        // TEST PUT, GET, REMOVE, and EXISTS
		        
		        cache.put(key, new ByteArrayInputStream(value.getBytes()));
		        
		        String returnValue = getStringFromInputStream(cache.get(key));
			
		        System.out.println(returnValue);
		        System.out.println(value);
		        
		        TestCase.assertEquals(value, returnValue);
		        TestCase.assertEquals(cache.exists(key), true);
		        
		        cache.clear(); // test clear
		        
		        TestCase.assertEquals(cache.exists(key), false);
		        
			}
		        
		}
		catch (ResourceException e) {
			e.printStackTrace();
		} 
		finally {
			
		}
		
	}
	
	@Test 
    public void repeatingKeyTest() {
        
        try {
                            
            final File tempFolder = new File("./target/test-files/temp-data-repeatingKeyTest");
            final File dataFolder = new File("./target/test-files/data-repeatingKeyTest");
            final File dataFolderItem = new File("./target/test-files/data-repeatingKeyTest/data");
            
            deleteRoot(tempFolder);
            deleteRoot(dataFolder);
            dataFolderItem.delete();
           
            TestCase.assertFalse(dataFolderItem.exists());
            
            tempFolder.mkdirs();
            dataFolder.mkdirs();
            
            final Cache<String, InputStream> fileCache = new FileHashCache(dataFolder, tempFolder);
            final Cache<String, InputStream> cache = new SynchronizedCache<String, InputStream>(fileCache);
            
            // create varying length strings by concatenation
            final String keyOrig = "abcdef0123456789";
            final String valueOrig = "abcdefghiasdsadsaa";
                        
            for(int i = 0; i < 500; i++) {
                
                final int random = (int) (Math.random() * 100);
                
                if(i % 100 == 0) {
                    
                    logger.debug("Size of data " + dataFolderItem.length());
                    
                    for(int j = 0; j < 100; j++) {
                        
                        final String key = StringUtilities.repeat(keyOrig, j);
                        
                        cache.remove(key);
                        
                        TestCase.assertEquals(cache.exists(key), false);
                        TestCase.assertNull(cache.get(key));
                        
                    }
                    
                }
                
                // TEST PUT, GET, REMOVE, and EXISTS
                
                final String value = StringUtilities.repeat(valueOrig, random);
                final String key = StringUtilities.repeat(keyOrig, random);
                
                cache.put(key, new ByteArrayInputStream(value.getBytes()));
                
                String returnValue = getStringFromInputStream(cache.get(key));
                
                TestCase.assertEquals(value, returnValue);
                TestCase.assertEquals(cache.exists(key), true);
                
            }
                   
        }
        catch (ResourceException e) {
            
            logger.error("ERROR:", e);
            TestCase.fail();
        } 
        
    }
	
}
