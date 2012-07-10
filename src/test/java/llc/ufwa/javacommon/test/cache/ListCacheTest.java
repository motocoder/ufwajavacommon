package llc.ufwa.javacommon.test.cache;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.ListCache;
import llc.ufwa.data.resource.cache.MemoryCache;

import org.junit.Test;

public class ListCacheTest {

    @Test
    public void testListCacheNormal() {

        try {
       
            final Cache<String, String> cache1 = new MemoryCache<String, String>();
            final Cache<String, String> cache2 = new MemoryCache<String, String>();
            final Cache<String, String> cache3 = new MemoryCache<String, String>();
            
            final List<Cache<String, String>> list = new ArrayList<Cache<String, String>>();
            
            list.add(cache1);
            list.add(cache2);
            list.add(cache3);
            
            final Cache<String, String> cache = new ListCache<String, String>(list, true);
            
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
    public void testListCachePullThrough() {

        try {
       
            final Cache<String, String> cache1 = new MemoryCache<String, String>();
            final Cache<String, String> cache2 = new MemoryCache<String, String>();
            final Cache<String, String> cache3 = new MemoryCache<String, String>();
            
            final List<Cache<String, String>> list = new ArrayList<Cache<String, String>>();
            
            list.add(cache1);
            list.add(cache2);
            list.add(cache3);
            
            final Cache<String, String> cache = new ListCache<String, String>(list, true);
            
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
                
                cache3.put("hi", "test");
                
                TestCase.assertTrue(cache.exists("hi"));
                TestCase.assertEquals("test", cache.get("hi"));
                TestCase.assertTrue(cache2.exists("hi"));
                TestCase.assertEquals("test", cache2.get("hi"));
                TestCase.assertTrue(cache1.exists("hi"));
                TestCase.assertEquals("test", cache1.get("hi"));
                
                cache2.put("hi1", "test1");
                cache1.put("hi2", "test2");
                
                final List<String> results2 = cache.getAll(keys);
                
                TestCase.assertEquals(results2.get(0), "test1");
                TestCase.assertEquals(results2.get(1), "test2");
                
                TestCase.assertNull(cache3.get("hi1"));
                TestCase.assertNull(cache3.get("hi2"));
                TestCase.assertNull(cache2.get("hi2"));
                
                TestCase.assertEquals("test1", cache1.get("hi1"));
                TestCase.assertEquals("test2", cache1.get("hi2"));
                
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
                
                cache.clear();
                
            }
            
            {
                cache3.put("3", "3");
                cache2.put("2", "2");
                cache1.put("1", "1");
                
                cache.get("1");
                cache.get("2");
                cache.get("3");
                
                TestCase.assertEquals("3", cache1.get("3"));
                TestCase.assertEquals("2", cache1.get("2"));
                TestCase.assertEquals("1", cache1.get("1"));
                
                TestCase.assertEquals("3", cache2.get("3"));
                TestCase.assertEquals("2", cache2.get("2"));
                TestCase.assertEquals(null, cache2.get("1"));
                
                TestCase.assertEquals("3", cache3.get("3"));
                TestCase.assertEquals(null, cache3.get("2"));
                TestCase.assertEquals(null, cache3.get("1"));
                
            }
            
        }
        catch(Exception e) {
            e.printStackTrace();
            TestCase.fail("should not get here");
        }
        
    }
    
    @Test
    public void testListCacheNotPullThrough() {

        try {
       
            final Cache<String, String> cache1 = new MemoryCache<String, String>();
            final Cache<String, String> cache2 = new MemoryCache<String, String>();
            final Cache<String, String> cache3 = new MemoryCache<String, String>();
            
            final List<Cache<String, String>> list = new ArrayList<Cache<String, String>>();
            
            list.add(cache1);
            list.add(cache2);
            list.add(cache3);
            
            final Cache<String, String> cache = new ListCache<String, String>(list, false);
            
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
                
                cache3.put("hi", "test");
                
                TestCase.assertTrue(cache.exists("hi"));
                TestCase.assertEquals("test", cache.get("hi"));
                TestCase.assertFalse(cache2.exists("hi"));
                TestCase.assertEquals(null, cache2.get("hi"));
                TestCase.assertFalse(cache1.exists("hi"));
                TestCase.assertEquals(null, cache1.get("hi"));
                
                cache2.put("hi1", "test1");
                cache1.put("hi2", "test2");
                
                final List<String> results2 = cache.getAll(keys);
                
                TestCase.assertEquals(results2.get(0), "test1");
                TestCase.assertEquals(results2.get(1), "test2");
                
                TestCase.assertNull(cache3.get("hi1"));
                TestCase.assertNull(cache3.get("hi2"));
                TestCase.assertNull(cache2.get("hi2"));
                
                TestCase.assertEquals(null, cache1.get("hi1"));
                TestCase.assertEquals("test2", cache1.get("hi2"));
                
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
                
                cache.clear();
                
            }
            
            {
                cache3.put("3", "3");
                cache2.put("2", "2");
                cache1.put("1", "1");
                
                cache.get("1");
                cache.get("2");
                cache.get("3");
                
                TestCase.assertEquals(null, cache1.get("3"));
                TestCase.assertEquals(null, cache1.get("2"));
                TestCase.assertEquals("1", cache1.get("1"));
                
                TestCase.assertEquals(null, cache2.get("3"));
                TestCase.assertEquals("2", cache2.get("2"));
                TestCase.assertEquals(null, cache2.get("1"));
                
                TestCase.assertEquals("3", cache3.get("3"));
                TestCase.assertEquals(null, cache3.get("2"));
                TestCase.assertEquals(null, cache3.get("1"));
                
            }
            
        }
        catch(Exception e) {
            e.printStackTrace();
            TestCase.fail("should not get here");
        }
        
    }
}
