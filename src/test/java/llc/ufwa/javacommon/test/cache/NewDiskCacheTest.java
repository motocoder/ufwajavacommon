package llc.ufwa.javacommon.test.cache;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.KeyEncodingCache;
import llc.ufwa.data.resource.cache.NewDiskCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;

import org.junit.Test;

public class NewDiskCacheTest {
    
    private static final byte [] TEN_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    
    @Test
    public void testDiskCache() {
        
        File root = new File("./temp1/");
        root.delete();
        
        try {
            
            final Cache<String, String> cache = 
                new ValueConvertingCache<String, String, byte []>(
                    new ValueConvertingCache<String, byte [], InputStream>(
                        new KeyEncodingCache<InputStream>(
                            new NewDiskCache(root, 500000, 50000)
                        ),
                        new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                    ),
                    new SerializingConverter<String>()
                );
            
            {
                
                TestCase.assertNull(cache.get("hi"));
                TestCase.assertFalse(cache.exists("hi"));
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("hi1");
                keys.add("hi2");
                
                final List<String> results = cache.getAll(keys);
                
                TestCase.assertEquals(2, results.size());
                TestCase.assertNull(results.get(0));
                TestCase.assertNull(results.get(1));
                
                cache.put("hi", "test");
                
                TestCase.assertTrue(cache.exists("hi"));
                TestCase.assertEquals("test", cache.get("hi"));
                
                cache.put("hi1", "test1");
                cache.put("hi2", "test2");
                
                final List<String> results2 = cache.getAll(keys);
                
                TestCase.assertEquals(results2.get(0), "test1");
                TestCase.assertEquals(results2.get(1), "test2");
                
                cache.clear();
                
            }
            
            {
                TestCase.assertNull(cache.get("hi"));
                TestCase.assertFalse(cache.exists("hi"));
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("hi1");
                keys.add("hi2");
                
                final List<String> results = cache.getAll(keys);
                
                TestCase.assertEquals(2, results.size());
                
                cache.clear();
                
            }
            
            {
                
                cache.put("hi", "test");
                
                TestCase.assertEquals("test", cache.get("hi"));
                
                cache.remove("hi");
                
                TestCase.assertNull(cache.get("hi"));
                
            }
            
        }
        catch(ResourceException e) {
            TestCase.fail("failed");
        }
        finally {
            root.delete();
        }
        
    }
    
    @Test 
    public void testMemoryCache() {
 
        try {
            
            File root = new File("./temp2/");
            
            root.delete();
       
            final Cache<String, String> cache = 
                new ValueConvertingCache<String, String, byte []>(
                    new ValueConvertingCache<String, byte [], InputStream>(
                        new KeyEncodingCache<InputStream>(
                            new NewDiskCache(root, -1, -1)
                        ),
                        new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                    ),
                    new SerializingConverter<String>()
                );
            
            {
                
                TestCase.assertNull(cache.get("hi"));
                TestCase.assertFalse(cache.exists("hi"));
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("hi1");
                keys.add("hi2");
                
                final List<String> results = cache.getAll(keys);
                
                TestCase.assertEquals(2, results.size());
                TestCase.assertNull(results.get(0));
                TestCase.assertNull(results.get(1));
                
                cache.put("hi", "test");
                
                TestCase.assertTrue(cache.exists("hi"));
                TestCase.assertEquals("test", cache.get("hi"));
                
                cache.put("hi1", "test1");
                cache.put("hi2", "test2");
                
                final List<String> results2 = cache.getAll(keys);
                
                TestCase.assertEquals(results2.get(0), "test1");
                TestCase.assertEquals(results2.get(1), "test2");
                
                cache.clear();
                
            }
            
            {
                TestCase.assertNull(cache.get("hi"));
                TestCase.assertFalse(cache.exists("hi"));
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("hi1");
                keys.add("hi2");
                
                final List<String> results = cache.getAll(keys);
                
                TestCase.assertEquals(2, results.size());
                
                cache.clear();
                
            }
            
            {
                
                cache.put("hi", "test");
                
                TestCase.assertEquals("test", cache.get("hi"));
                
                cache.remove("hi");
                
                TestCase.assertNull(cache.get("hi"));
                
            }
            
        }
        catch(Exception e) {
            
            e.printStackTrace();
            TestCase.fail("should not get here");
            
        }
        
    }
    
    @Test
    public void testMemoryCacheSizeStuff() {
        
        File root = new File("./temp3/");
        
        root.delete();
        
        final Cache<String, byte []> cache = 
            new ValueConvertingCache<String, byte [], InputStream>(
                new KeyEncodingCache<InputStream>(
                    new NewDiskCache(root, 20, -1)
                ),
                new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
            );
//        
//        final Cache<String, String> cache = 
//                new MemoryCache<String, String>(
//                        new StringSizeConverter() {
//                        }, 
//                        100);
        
        try {
            
            cache.put("1", TEN_BYTES);
        
            Thread.sleep(50);
        
            cache.put("2", TEN_BYTES);
            
            Thread.sleep(50);
            
            cache.put("3", TEN_BYTES);
            
            Thread.sleep(50);
            
            cache.put("4", TEN_BYTES);
            
            Thread.sleep(50);
            
            cache.put("5", TEN_BYTES);
            
            
        
        }
        catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        try {
            
            TestCase.assertNull(cache.get("1"));
            TestCase.assertNull(cache.get("2"));
            TestCase.assertNull(cache.get("3"));
            TestCase.assertNotNull(cache.get("4"));
            TestCase.assertNotNull(cache.get("5"));
            
        } 
        catch (ResourceException e) {
            
            TestCase.fail("cant get here");
            e.printStackTrace();
            
        }
        
    }
    
    @Test
    public void testExpires() {
        
        File root = new File("./temp4/");
        
        root.delete();
        
        final Cache<String, byte []> cache = 
            new ValueConvertingCache<String, byte [], InputStream>(
                new KeyEncodingCache<InputStream>(
                    new NewDiskCache(root, 20, 1000)
                ),
                new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
            );
//        
//        final Cache<String, String> cache = 
//                new MemoryCache<String, String>(
//                        new StringSizeConverter() {
//                        }, 
//                        100);
        
        try {
            
            cache.put("1", TEN_BYTES);
        
            Thread.sleep(50);
        
            cache.put("2", TEN_BYTES);
            
            Thread.sleep(50);
            
            cache.put("3", TEN_BYTES);
            
            Thread.sleep(50);
            
            cache.put("4", TEN_BYTES);
            
            Thread.sleep(50);
            
            cache.put("5", TEN_BYTES);
            
            Thread.sleep(1200);
        
        }
        catch (InterruptedException e1) {
            TestCase.fail();
        }
        
        try {
            
            TestCase.assertNull(cache.get("1"));
            TestCase.assertNull(cache.get("2"));
            TestCase.assertNull(cache.get("3"));
            TestCase.assertNull(cache.get("4"));
            TestCase.assertNull(cache.get("5"));
            
        } 
        catch (ResourceException e) {
            
            TestCase.fail("cant get here");
            e.printStackTrace();
            
        }
    }
}
