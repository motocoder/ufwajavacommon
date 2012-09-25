package llc.ufwa.javacommon.test.resource;

import java.util.concurrent.Executors;

import junit.framework.TestCase;
import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.MemoryCache;
import llc.ufwa.data.resource.loader.CachedParallelResourceLoader;
import llc.ufwa.data.resource.loader.ResourceEvent;
import llc.ufwa.data.resource.loader.ResourceLoader;
import llc.ufwa.javacommon.test.JavaCommonLimitingExecutorService;

import org.junit.Test;

public class CachedParallelResourceLoaderForResourceLoaderTest {
    
    @Test
    public void testGetCache() {
        
        try {
            
            final Cache<String, String> internal = new MemoryCache<String, String>();
            
            internal.put("hi", "hi");
            
            final MemoryCache<String, Boolean> searchCache = new MemoryCache<String, Boolean>();
            final MemoryCache<String, String> cache = new MemoryCache<String, String>();
            
            final ResourceLoader<String, String> parallelLoader = 
                new CachedParallelResourceLoader<String, String>(
                    internal,
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10), 10),
                    Executors.newFixedThreadPool(10),
                    Executors.newFixedThreadPool(10),
                    10,
                    "",
                    cache, 
                    searchCache
                );
            
            //check first attempt
            String response = parallelLoader.get("hi");
            
            TestCase.assertEquals("hi", response);
            TestCase.assertEquals("hi", cache.get("hi"));
            TestCase.assertTrue(searchCache.get("hi"));
            
            internal.clear();
            
            //check to make sure caching working
            
            response = parallelLoader.get("hi");
            
            TestCase.assertEquals("hi", response);
            TestCase.assertEquals("hi", cache.get("hi"));
            TestCase.assertTrue(searchCache.get("hi"));
            
            searchCache.clear();
            
            //make sure search of cache when searchCache empty.
            
            response = parallelLoader.get("hi");
            
            TestCase.assertEquals("hi", response);
            TestCase.assertEquals("hi", cache.get("hi"));
            TestCase.assertNull(searchCache.get("hi"));
            
            cache.clear();
            
            //test to make sure it gets null again.
            
            response = parallelLoader.get("hi");
            
            TestCase.assertNull(response);
            TestCase.assertNull(cache.get("hi"));
            TestCase.assertFalse(searchCache.get("hi"));
            
        
        }
        catch(Exception e) {
            e.printStackTrace();
            TestCase.fail("Should not have thrown exception");
        }
        
        
         
    }
    
    @Test
    public void testGetParallelCache() {
        
        try {
            
            final Cache<String, String> internal = new MemoryCache<String, String>();
            
            internal.put("hi", "hi");
            
            final ParallelControl<Object> control = new ParallelControl<Object>();
            
            final MemoryCache<String, Boolean> searchCache = new MemoryCache<String, Boolean>();
            final MemoryCache<String, String> cache = new MemoryCache<String, String>();
            
            final CachedParallelResourceLoader<String, String> parallelLoader = 
                new CachedParallelResourceLoader<String, String>(
                    internal,
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                    Executors.newFixedThreadPool(10),
                    Executors.newFixedThreadPool(10),
                    10,
                    "",
                    cache,
                    searchCache
                );
            
            //check first attempt
            parallelLoader.getParallel(
                new Callback<Object, ResourceEvent<String>>() {

                    @Override
                    public boolean call(Object source, ResourceEvent<String> value) {
                        
                        System.out.println("Value " + value.getVal());
                        
                        control.setValue(value.getVal());
                        control.unBlockOnce();
                        
                        return false;
                        
                    }
                    
                },
                "hi"
            );
            
            control.blockOnce();
            
            String response = (String) control.getValue();
            
            TestCase.assertEquals("hi", response);
            TestCase.assertEquals("hi", cache.get("hi"));
            TestCase.assertTrue(searchCache.get("hi"));
            
            internal.clear();
            
            //check to make sure caching working
            
            parallelLoader.getParallel(
                new Callback<Object, ResourceEvent<String>>() {

                    @Override
                    public boolean call(Object source, ResourceEvent<String> value) {
                        
                        control.setValue(value.getVal());
                        control.unBlockOnce();
                        
                        return false;
                        
                    }
                    
                },
                "hi"
            );
            
            control.blockOnce();
            
            response = (String) control.getValue();
            
            TestCase.assertEquals("hi", response);
            TestCase.assertEquals("hi", cache.get("hi"));
            TestCase.assertTrue(searchCache.get("hi"));
            
            searchCache.clear();
            
            //make sure search of cache when searchCache empty.
            
            parallelLoader.getParallel(
                new Callback<Object, ResourceEvent<String>>() {

                    @Override
                    public boolean call(Object source, ResourceEvent<String> value) {
                        
                        control.setValue(value.getVal());
                        control.unBlockOnce();
                        
                        return false;
                        
                    }
                    
                },
                "hi"
            );
            
            control.blockOnce();
            
            response = (String) control.getValue();
            
            TestCase.assertEquals("hi", response);
            TestCase.assertEquals("hi", cache.get("hi"));
            TestCase.assertNull(searchCache.get("hi"));
            
            cache.clear();
            
            //test to make sure it gets null again.
            
            parallelLoader.getParallel(
                new Callback<Object, ResourceEvent<String>>() {

                    @Override
                    public boolean call(Object source, ResourceEvent<String> value) {
                        
                        control.setValue(value.getVal());
                        control.unBlockOnce();
                        
                        return false;
                        
                    }
                    
                },
                "hi"
            );
            
            control.blockOnce();
            
            response = (String) control.getValue();
            
            TestCase.assertNull(response);
            TestCase.assertNull(cache.get("hi"));
            TestCase.assertFalse(searchCache.get("hi"));
            
        
        }
        catch(Exception e) {
            e.printStackTrace();
            TestCase.fail("Should not have thrown exception");
        }
        
        
         
    }
    
    @Test
    public void testExistsCache() {
        
        try {
            
            final Cache<String, String> internal = new MemoryCache<String, String>();
            
            internal.put("hi", "hi");
            
            final ParallelControl<Object> control = new ParallelControl<Object>();
            
            final MemoryCache<String, Boolean> searchCache = new MemoryCache<String, Boolean>();
            final MemoryCache<String, String> cache = new MemoryCache<String, String>();
            
            final ResourceLoader<String, String> parallelLoader = 
                new CachedParallelResourceLoader<String, String>(
                    internal,
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                    Executors.newFixedThreadPool(10),
                    Executors.newFixedThreadPool(10),
                    10,
                    "",
                    cache,
                    searchCache
                );
            
            //check first attempt
            boolean response = parallelLoader.exists("hi");
            
            TestCase.assertEquals(true, response);
            TestCase.assertNull(cache.get("hi"));
            TestCase.assertTrue(searchCache.get("hi"));
            
            internal.clear();
            
            //check to make sure caching working
            
            response = parallelLoader.exists("hi");
            
            TestCase.assertEquals(true, response);
            TestCase.assertNull(cache.get("hi"));
            TestCase.assertTrue(searchCache.get("hi"));
            
            searchCache.clear();
            
            //make sure search of cache when searchCache empty.
            
            response = parallelLoader.exists("hi");
            
            TestCase.assertFalse(response);
            TestCase.assertNull(cache.get("hi"));
            TestCase.assertFalse(searchCache.get("hi"));
            
            cache.clear();
            
            //test to make sure it gets null again.
            
            response = parallelLoader.exists("hi");
            
            TestCase.assertFalse(response);
            TestCase.assertNull(cache.get("hi"));
            TestCase.assertFalse(searchCache.get("hi"));
            
        
        }
        catch(Exception e) {
            e.printStackTrace();
            TestCase.fail("Should not have thrown exception");
        }
        
        
         
    }
    
    @Test
    public void testExistsParallelCache() {
        
        try {
            
            final Cache<String, String> internal = new MemoryCache<String, String>();
            
            internal.put("hi", "hi");
            
            final ParallelControl<Object> control = new ParallelControl<Object>();
            
            final MemoryCache<String, Boolean> searchCache = new MemoryCache<String, Boolean>();
            final MemoryCache<String, String> cache = new MemoryCache<String, String>();
            
            final CachedParallelResourceLoader<String, String> parallelLoader = 
                new CachedParallelResourceLoader<String, String>(
                    internal,
                    new JavaCommonLimitingExecutorService(Executors.newFixedThreadPool(10),10),
                    Executors.newFixedThreadPool(10),
                    Executors.newFixedThreadPool(10),
                    10,
                    "",
                    cache,
                    searchCache
                );
            
            //check first attempt
            parallelLoader.existsParallel(
                new Callback<Object, ResourceEvent<Boolean>>() {

                    @Override
                    public boolean call(Object source, ResourceEvent<Boolean> value) {
                        
                        control.setValue(value.getVal());
                        control.unBlockOnce();
                        
                        return false;
                        
                    }
                    
                },
                "hi"
            );
            
            control.blockOnce();
            
            boolean response = (Boolean) control.getValue();
            
            TestCase.assertEquals(true, response);
            TestCase.assertNull(cache.get("hi"));
            TestCase.assertTrue(searchCache.get("hi"));
            
            internal.clear();
            
            //check to make sure caching working
            
            parallelLoader.existsParallel(
                new Callback<Object, ResourceEvent<Boolean>>() {

                    @Override
                    public boolean call(Object source, ResourceEvent<Boolean> value) {
                        
                        control.setValue(value.getVal());
                        control.unBlockOnce();
                        
                        return false;
                        
                    }
                    
                },
                "hi"
            );
            
            control.blockOnce();
            
            response = (Boolean) control.getValue();
            
            TestCase.assertEquals(true, response);
            TestCase.assertNull(cache.get("hi"));
            TestCase.assertTrue(searchCache.get("hi"));
            
            searchCache.clear();
            
            //make sure search of cache when searchCache empty.
            
            parallelLoader.existsParallel(
                new Callback<Object, ResourceEvent<Boolean>>() {

                    @Override
                    public boolean call(Object source, ResourceEvent<Boolean> value) {
                        
                        control.setValue(value.getVal());
                        control.unBlockOnce();
                        
                        return false;
                        
                    }
                    
                },
                "hi"
            );
            
            control.blockOnce();
            
            response = (Boolean) control.getValue();
            
            TestCase.assertEquals(false, response);
            TestCase.assertNull(cache.get("hi"));
            TestCase.assertFalse(searchCache.get("hi"));
            
            cache.clear();
            
            //test to make sure it gets null again.
            
            parallelLoader.existsParallel(
                new Callback<Object, ResourceEvent<Boolean>>() {

                    @Override
                    public boolean call(Object source, ResourceEvent<Boolean> value) {
                        
                        control.setValue(value.getVal());
                        control.unBlockOnce();
                        
                        return false;
                        
                    }
                    
                },
                "hi"
            );
            
            control.blockOnce();
            
            response = (Boolean) control.getValue();
            
            TestCase.assertFalse(response);
            TestCase.assertNull(cache.get("hi"));
            TestCase.assertFalse(searchCache.get("hi"));
            
        
        }
        catch(Exception e) {
            e.printStackTrace();
            TestCase.fail("Should not have thrown exception");
        }
        
        
         
    }

}
