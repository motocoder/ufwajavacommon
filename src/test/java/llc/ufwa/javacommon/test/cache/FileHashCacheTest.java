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
	        final String key3 = "adatiwefwwawfwfdfdsgdfgsdgasdd982y4d8913y894d012ny4d92803u4d89023mnd4038912d4ymn09128dyhmn9821d4yn328yd4n289314yndm9812ynd409jkjlafoasudf8904uy04uf4309u0oidsnhfioashd9834jh0q4f08943qhlq34hf89034fh48fih3fsds";
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test 
	public void multiThreadedTest() {
		
		for (int x = 0; x < 25; x++) {
			
			Thread thread = new Thread() {
			
				public void run() {
					
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
				        
				        // create varying length strings by concatenation
				        String key = "abcdefghijklmnopqrstuvwxyz0123456789";
				        String value = "abcdefghijklmnopqrstuvwxyz0123456789";
				        Random random = new Random();
						int iterations = random.nextInt(5);
				        
				        for (int i = 0; i < iterations; i++) {
				        	key = key.concat(key);
				        	value = value.concat(value);
				        }
				        
				        logger.info("putting value of length: " + value.length());
				        
				        String returnValue;
				        
				        // TEST PUT, GET, REMOVE, and EXISTS
				        
				        cache.put(key, new ByteArrayInputStream(value.getBytes()));
				        
				        InputStream baos = null;
				        
				        baos = cache.get(key);
						
				        returnValue = getStringFromInputStream(baos);
				        
				        TestCase.assertEquals(value, returnValue);
				        TestCase.assertEquals(cache.exists(key), true);
				        
				        cache.clear(); // test clear
				        
				        TestCase.assertEquals(cache.exists(key), false);
				        
				        baos.close();
				        
					}
					catch (ResourceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        
				}
		        
			};
			
			thread.start();
			
		}
		
	}
	
	
	@Test 
	public void varyingKeyLengthTest() {
		
		try {
			
			for (int x = 0; x < 25; x++) {
				
		        final File tempFolder = new File("./target/test-files/temp-data");
		        final File dataFolder = new File("./target/test-files/data");
		        final File dataFolderItem = new File("./target/test-files/data/data");
		        
		        deleteRoot(tempFolder);
		        deleteRoot(dataFolder);
		        dataFolderItem.delete();
		        
		        tempFolder.mkdirs();
		        dataFolder.mkdirs();
		        
		        final FileHashCache cache = new FileHashCache(dataFolderItem, tempFolder);
		        
		        // create varying length strings by concatenation
		        String key = "abcdefghijklmnopqrstuvwxyz0123456789";
		        String value = "abcdefghijklmnopqrstuvwxyz0123456789";
		        Random random = new Random();
				int iterations = random.nextInt(10);
		        
		        for (int i = 0; i < iterations; i++) {
		        	key = key.concat(key);
		        	value = value.concat(value);
		        }
		        
		        logger.info("putting value of length: " + value.length());
		        
		        String returnValue;
		        
		        // TEST PUT, GET, REMOVE, and EXISTS
		        
		        cache.put(key, new ByteArrayInputStream(value.getBytes()));
		        
		        InputStream baos = null;
		        
		        baos = cache.get(key);
				
		        returnValue = getStringFromInputStream(baos);
		        
		        TestCase.assertEquals(value, returnValue);
		        TestCase.assertEquals(cache.exists(key), true);
		        
		        cache.clear(); // test clear
		        
		        TestCase.assertEquals(cache.exists(key), false);
		        
		        baos.close();
		        
			}
		        
		}
		catch (ResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
