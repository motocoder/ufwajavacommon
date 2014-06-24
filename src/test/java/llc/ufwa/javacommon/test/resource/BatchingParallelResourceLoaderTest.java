package llc.ufwa.javacommon.test.resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.LimitingExecutorServiceFactory;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.StringSizeConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.MemoryCache;
import llc.ufwa.data.resource.cache.SynchronizedCache;
import llc.ufwa.data.resource.loader.BatchingParallelResourceLoader;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.data.resource.loader.ParallelResourceLoader;
import llc.ufwa.data.resource.loader.ParallelResourceLoaderImpl;
import llc.ufwa.data.resource.loader.ResourceEvent;
import llc.ufwa.data.resource.loader.ResourceLoader;
import llc.ufwa.util.StopWatch;

import org.junit.Test;

public class BatchingParallelResourceLoaderTest {
    
    @Test
    public void testBatchingParallelResourceLoaderStress() {
        
        final Random rand = new Random(); 
        final HashSet<String> hashset = new HashSet<String>();
        
        final ResourceLoader<String, String> internal = 
                new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException { 
                
                try {
                    Thread.sleep(100);
                } 
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                return key;
                
            }
            
        };
        
        //final ParallelControl<Object> control = new ParallelControl<Object>();
         
        final ParallelResourceLoader<String, String> parallelLoader = 
            new ParallelResourceLoaderImpl<String, String>(
                internal, 
                LimitingExecutorServiceFactory.createExecutorService(
                    Executors.newFixedThreadPool(10), 
                    Executors.newFixedThreadPool(100),
                    10
                ),
                Executors.newFixedThreadPool(10), 
                10, 
                ""
            );
        
        final Cache<String, Boolean> searchCache = 
            new SynchronizedCache<String, Boolean>(
                new MemoryCache<String, Boolean>()
            );
        
        final Cache<String, String> cache = 
            new SynchronizedCache<String, String>(
                new MemoryCache<String, String>(new StringSizeConverter(), 10000)
            );
    
        final List<String> positions = new ArrayList<String>();
        final List<String> keys = new ArrayList<String>();
        
        for (int x = 0; x < 1000; x++) {
            
            final String rando2 = String.valueOf(rand.nextInt());
            positions.add(rando2); 
            keys.add(rando2);
            
        }
        
        final BatchingParallelResourceLoader<String, String> batched =
                new BatchingParallelResourceLoader<String, String>(
                    parallelLoader,
                    5,
                    5,
                    searchCache,
                    cache,
                    positions
                );
        
        for (int x = 0; x < 50; x++){
            
            new Thread() {
                
                @Override
                public void run() {
                        
                    final int i = rand.nextInt()  % keys.size();
                    final String key = keys.get(i);
                    
                    try {
                        
                        batched.getParallel(
                            new Callback <Object, ResourceEvent <String> > () {

                                @Override
                                public Object call(
                                    final ResourceEvent<String> value
                                ) {
                                    
                                    try {
                                        cache.put(key, "lkjdslkjeiodckdslkdjsflkcmwe;eoiweorcomeiooaijoeckmo" + i);
                                    } catch (ResourceException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    return false;
                                    
                                }
                                
                            } , 
                            key
                        );
                        
                    }
                    catch (ResourceException e) {
                        e.printStackTrace();
                    }
                    
                    synchronized(hashset) {
                        
                        hashset.add(String.valueOf(i));
                        
                        if(hashset.size() == 50) {
                            hashset.notifyAll();
                        }
                        
                    }
                    
                }
                
            }.start();
            
        }
        
        for (int x = 0; x < 50; x++){
            
            final int i = x;
            
            new Thread() {
                
                @Override
                public void run() {
                    
                    Random rand = new Random(); 
                    int size = 1000;
                    
                    for (int x = 0; x < size; x++) {
                        
                        final int rando2 = rand.nextInt();
                        final String key = String.valueOf(rand.nextInt());

                        try {
                            cache.put(key, String.valueOf(rando2) + "dchvjhgvggvjgvghvjhjgvhgfcgfjgsdfghjkdfgvhjsdfghsdfghsdfg");
                        } catch (ResourceException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        
                        try {
                            TestCase.assertEquals(cache.get(key), String.valueOf(rando2) + "dchvjhgvggvjgvghvjhjgvhgfcgfjgsdfghjkdfgvhjsdfghsdfghsdfg");
                        } 
                        catch (ResourceException e) {
                            e.printStackTrace();
                        }
                        
                        try {
                            cache.remove(key);
                        } catch (ResourceException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        
                    }
                     
                    final List<String> keys2 = new ArrayList<String>();
                    
                    for (int y=0; y < size; y++) {  
                        
                        keys2.add(String.valueOf(y));
                        try {
                            cache.put(String.valueOf(y), y + "asdfghjsdfghjksdfghjkdfghjdfghjdfgbhjsdfghjsdfghjsdfghjdfghjdfghjksdfghjdfghjdfgh");
                        } catch (ResourceException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        
                        try {
                            cache.get(String.valueOf(y));
                        } 
                        catch (ResourceException e) {
                            e.printStackTrace();
                        }
                        
                    }
                    
                    try {
                        cache.getAll(keys2);
                    } 
                    catch (ResourceException e) {
                        e.printStackTrace();
                    }
                    
                    synchronized(hashset) {
                        
                        hashset.add(String.valueOf(i + 50));
                        
                        if(hashset.size() == 100) { 
                            hashset.notifyAll();
                        }
                        
                    }
                    
                }
                
            }.start();
            
        }
        
        final StopWatch stop = new StopWatch();
        
        stop.start();
        
        synchronized(hashset) {
            
            int last = 0;
            int sameFor = 0;
            
            while ((hashset.size() < 100) && sameFor < 7){
                
                try {
                    hashset.wait(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                if(last == hashset.size()) {
                    sameFor++;
                }
                
                last = hashset.size();
                
                
                
            }
             
        }
        
    }
    
    @Test
    public void testBatchingParallelResourceLoader() {
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException { 
                return key;
            }
            
        };
         
        final ParallelResourceLoader<String, String> parallelLoader = 
            new ParallelResourceLoaderImpl<String, String>(
                internal, 
                LimitingExecutorServiceFactory.createExecutorService(
                    Executors.newFixedThreadPool(10), 
                    Executors.newFixedThreadPool(100),
                    10
                    ),
                    Executors.newFixedThreadPool(10), 
                    10, 
                    ""
                );
        
        final Cache<String, Boolean> searchCache = new SynchronizedCache<String, Boolean>(new MemoryCache<String, Boolean>());
        final Cache<String, String> cache = new SynchronizedCache<String, String>(new MemoryCache<String, String>());
        
        final List<String> positions = new ArrayList<String>();
        
        positions.add("1");
        positions.add("2");
        positions.add("3");
        positions.add("4");
        positions.add("5");
        positions.add("6");
        positions.add("7");
        positions.add("8");
        positions.add("9");
        positions.add("10");
        positions.add("11");
        positions.add("12");
        positions.add("13");
        positions.add("14");
        positions.add("15");
        positions.add("16");
        positions.add("17");
        positions.add("18");
        positions.add("19");
        positions.add("20");
        positions.add("21");
        positions.add("22");
        positions.add("23");
        positions.add("24");
        positions.add("25");
        positions.add("26");
        positions.add("27");
        positions.add("28");
        positions.add("29");
        positions.add("30");
        
        final BatchingParallelResourceLoader<String, String> batched =
            new BatchingParallelResourceLoader<String, String>(
                parallelLoader,
                5,
                5,
                searchCache,
                cache,
                positions
            );
        
        try {
            
            batched.getParallel(new Callback<Object, ResourceEvent<String>>() {

                @Override
                public Object call(ResourceEvent<String> value) {
                    return null;
                }}, "3");
        
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                TestCase.fail();
            }
            
            TestCase.assertEquals("1", cache.get("1"));
            TestCase.assertTrue(searchCache.get("1"));
            
            TestCase.assertEquals("2", cache.get("2"));
            TestCase.assertTrue(searchCache.get("2"));
            
            TestCase.assertEquals("3", cache.get("3"));
            TestCase.assertTrue(searchCache.get("3"));
            
            TestCase.assertEquals("4", cache.get("4"));
            TestCase.assertTrue(searchCache.get("4"));
            
            TestCase.assertEquals("5", cache.get("5"));
            TestCase.assertTrue(searchCache.get("5"));
            
            TestCase.assertEquals("6", cache.get("6"));
            TestCase.assertTrue(searchCache.get("6"));
            
            TestCase.assertEquals("7", cache.get("7"));
            TestCase.assertTrue(searchCache.get("7"));
            
            TestCase.assertEquals("8", cache.get("8"));
            TestCase.assertTrue(searchCache.get("8"));
        } 
        catch (ResourceException e) {
            TestCase.fail();
        }
        
    }

}
