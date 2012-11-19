package llc.ufwa.javacommon.test.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import llc.ufwa.data.beans.Entry;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.ExpiringCache;
import llc.ufwa.data.resource.cache.MemoryCache;
import llc.ufwa.data.resource.cache.SynchronizedCache;
import llc.ufwa.util.StopWatch;

import org.junit.Test;

public class ExpiringCacheTest {
    
    @Test 
    public void testExpiringCacheMultiThreaded() {
        
        //final ParallelControl<String> control1 = new ParallelControl<String>();
         
        final StopWatch watch = new StopWatch();
        watch.start();
        
        final HashSet<String> completedThreads = new HashSet<String>();
        
        final Cache<String, String> cache = 
                new SynchronizedCache<String, String>(
                    new ExpiringCache<String, String>(
                        new MemoryCache<String, String>()
                    ,
                    100,
                    100
                )
            ); 
        
        final int THREAD_COUNT = 50;
            
        for (int x = 0; x < THREAD_COUNT; x++){
            
            final int i = x;
            
            new Thread() {
                
                @Override
                public void run() {
                    
                    try {
                        
                        // just add/remove a bunch of stuff.
                        final Random rand = new Random(); 
                        final int size = 5000;
                        
                        final List<String> keys = new ArrayList<String>();
                        
                        for (int x = 0; x < size; x++) {  
                            
                            int rando2 = rand.nextInt();
                            String key = String.valueOf(rand.nextInt());
                            
                            cache.put(key, String.valueOf(rando2));
                            keys.add(key);
                            
                            cache.get(key); 
                           
                            cache.remove(key);
                            
                        } 
                        
                        //add/get a bunch of stuff
                        final List<String> keys2 = new ArrayList<String>();
                        
                        for (int x=0; x < size; x++) {  
                            
                            keys2.add(String.valueOf(x));
                            cache.put(String.valueOf(x), String.valueOf(x));
                            
                        }
                        
                        final List<String> results = cache.getAll(keys2);
                        
                        completedThreads.add(String.valueOf(i)); //we are done, tell stuff it has completed.
                        
                    }
                    catch(ResourceException e) {
                        TestCase.fail("failed");
                    }
                    
                }
            }.start();
        }
        
        while(completedThreads.size() < THREAD_COUNT && watch.getTime() < 10000) {
            try {
                Thread.sleep(10);
            } 
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        TestCase.assertEquals(THREAD_COUNT, completedThreads.size());
                
    } 
    
//	@Test
//	public void testExpiringCacheTime() { //CA
//		
//	    Random rand = new Random(); 
//		long max = -1;
//		long min = 10000;
//	
//		int rando; 
//		int rando2;
//		
//		try {
//		    
//		    final StopWatch watch = new StopWatch();
//            watch.start();
//            
//            while(true) {
//			 
//			    final Cache<String, String> cache = 
//		            new SynchronizedCache<String, String>(
//		                new ExpiringCache<String, String>(
//		                    new MemoryCache<String, String>()
//		                , 
//		                100,
//		                100
//		            )
//		        ); 
//			    
//			    final List<String> keys = new ArrayList<String>();
//			    int size = rand.nextInt(1000);
//			    
//                for (int x=0; x < size; x++) {
//			        rando = rand.nextInt();
//			        rando2 = rand.nextInt();
//			        cache.put(String.valueOf(rando), String.valueOf(rando2));
//			        keys.add(String.valueOf(rando));
//			    }
//				
//                List<String> results;
//                
//                while (true) {
//                    
//                    final StopWatch watch2 = new StopWatch();
//                    watch2.start();
//                    
//                    results = cache.getAll(keys);
//                    
//                    Set<String> uniques = new HashSet<String>(results);
//                    
//                    String test = cache.get("");
//                    
//                    if (watch2.getTime() > max){
//    				    max = watch2.getTime();
//    				}
//    				
//    				if (watch2.getTime() < min){
//                        min = watch2.getTime();
//                    }
//    				
//    				uniques.remove(null);
//                    
//                    if (uniques.size() == 0) {
//                        break;
//                    }
//                    
//                    Thread.sleep(1);
//                    
//                }
//                
//				System.out.println("The total time was " + String.valueOf(watch.getTime()) + 
//			        " and the max clear time was " + max + 
//			        " and the min clear time was "+ min);
//			
//			}
//			
//			
//		}
//		
//		catch(Exception e) {
//            TestCase.fail("should not get here");
//        }
//		
//	}
	
    @Test
    public void testExpiringCacheSimply() {
    
        final Cache<String, String> cache = new ExpiringCache<String, String>(new MemoryCache<String, String>(), 10000, 10000);
        
        try {
            {
                
                TestCase.assertNull(cache.get("hi"));
                TestCase.assertFalse(cache.exists("hi"));
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("hi1");
                keys.add("hi2");
                
                final List<String> results = cache.getAll(keys);
                
                TestCase.assertEquals(2, results.size());
                
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
            TestCase.fail("should not get here");
        }

    }
    
    @Test
    public void testExpirationShortCleanup() {
        
        try {
            
            final Cache<String, String> cache = new ExpiringCache<String, String>(new MemoryCache<String, String>(), 100, 10);
                
            cache.put("hi", "test");
            TestCase.assertEquals("test", cache.get("hi"));
            
            try {
                Thread.sleep(110);
            } 
            catch (InterruptedException e) {
                TestCase.fail("Failed");
            }
            
            TestCase.assertNull(cache.get("hi"));
            
        }
        catch(Exception e) {
            TestCase.fail("should not get here");
        }
        
    }
    
    @Test
    public void testExpirationLongCleanup() {
        
        try {
            
            final Cache<String, String> cache = new ExpiringCache<String, String>(new MemoryCache<String, String>(), 100, 1000);
                
            cache.put("hi", "test");
            TestCase.assertEquals("test", cache.get("hi"));
            
            try {
                Thread.sleep(110);
            } 
            catch (InterruptedException e) {
                TestCase.fail("Failed");
            }
            
            TestCase.assertNull(cache.get("hi"));
        
        }
        catch(Exception e) {
            TestCase.fail("should not get here");
        }
            
    }
    
    @Test
    public void testGetAllExpirationLongCleanup() {
        
        try {
            
            final Cache<String, String> cache = new ExpiringCache<String, String>(new MemoryCache<String, String>(), 100, 1000);
                
            final List<Entry<String, String>> entries = new ArrayList<Entry<String, String>>();
            
            entries.add(new Entry<String, String>("hi1", "test1"));
            entries.add(new Entry<String, String>("hi2", "test2"));
            entries.add(new Entry<String, String>("hi3", "test3"));
            
            final List<String> keys = new ArrayList<String>();
            
            for(Entry<String, String> entry : entries) {
                
                keys.add(entry.getKey());
                cache.put(entry.getKey(), entry.getValue());
                
            }
            
            List<String> values = cache.getAll(keys);
            
            for(int i = 0; i < keys.size(); i++) {
                TestCase.assertEquals(values.get(i), entries.get(i).getValue());
            }
            
            try {
                Thread.sleep(160);
            } 
            catch (InterruptedException e) {
                TestCase.fail("Failed");
            }
            
            values = cache.getAll(keys);
            
            for(int i = 0; i < keys.size(); i++) {
                TestCase.assertNull(values.get(i));
            }
            
            for(Entry<String, String> entry : entries) {
                cache.put(entry.getKey(), entry.getValue());
            }
            
            try {
                Thread.sleep(110);
            } 
            catch (InterruptedException e) {
                TestCase.fail("Failed");
            }
            
            cache.put("hi1", "test1");
            
            values = cache.getAll(keys);
            
            for(int i = 0; i < keys.size(); i++) {
                
                if(keys.get(i).equals("hi1")) {
                    TestCase.assertEquals(values.get(i), entries.get(i).getValue());
                }
                else {
                    TestCase.assertNull(values.get(i));
                }
                
            }
        
        }
        catch(Exception e) {
            TestCase.fail("should not get here");
        }
        
    }
    
    @Test
    public void testGetAllExpirationShortCleanup() {
        
        try {
        
            final Cache<String, String> cache = new ExpiringCache<String, String>(new MemoryCache<String, String>(), 100, 10);
    
            final List<Entry<String, String>> entries = new ArrayList<Entry<String, String>>();
            
            entries.add(new Entry<String, String>("hi1", "test1"));
            entries.add(new Entry<String, String>("hi2", "test2"));
            entries.add(new Entry<String, String>("hi3", "test3"));
            
            final List<String> keys = new ArrayList<String>();
            
            for(Entry<String, String> entry : entries) {
                
                keys.add(entry.getKey());
                cache.put(entry.getKey(), entry.getValue());
                
            }
            
            List<String> values = cache.getAll(keys);
            
            for(int i = 0; i < keys.size(); i++) {
                TestCase.assertEquals(values.get(i), entries.get(i).getValue());
            }
            
            try {
                Thread.sleep(110);
            } 
            catch (InterruptedException e) {
                TestCase.fail("Failed");
            }
            
            values = cache.getAll(keys);
            
            for(int i = 0; i < keys.size(); i++) {
                TestCase.assertNull(values.get(i));
            }
            
            for(Entry<String, String> entry : entries) {
                cache.put(entry.getKey(), entry.getValue());
            }
            
            try {
                Thread.sleep(110);
            } 
            catch (InterruptedException e) {
                TestCase.fail("Failed");
            }
            
            cache.put("hi1", "test1");
            
            values = cache.getAll(keys);
            
            for(int i = 0; i < keys.size(); i++) {
                
                if(keys.get(i).equals("hi1")) {
                    TestCase.assertEquals(values.get(i), entries.get(i).getValue());
                }
                else {
                    TestCase.assertNull(values.get(i));
                }
                
            }  
        
        }
        catch(Exception e) {
            TestCase.fail("should not get here");
        }
        
    }
    
    @Test
    public void testNegInput() {
    
        final Cache<String, String> cache = new ExpiringCache<String, String>(new MemoryCache<String, String>(), 10000, 10000);
            
        {
           
            try {
                
                cache.get(null);
                TestCase.fail("Shouldn't have gotten here");
                
            }
            catch(Exception e) {
                //expected behavior
            }
            
            try {
                
                cache.exists(null);
                TestCase.fail("Shouldn't have gotten here");
                
            }
            catch(Exception e) {
                //expected behavior
            }
            
            try {
                
                cache.getAll(null);
                TestCase.fail("Shouldn't have gotten here");
                
            }
            catch(Exception e) {
                //expected behavior
            }
            
            try {
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("hi");
                keys.add(null);
                
                cache.getAll(keys);
                TestCase.fail("Shouldn't have gotten here");
                
            }
            catch(Exception e) {
                //expected behavior
            }
            
            try {
                
                cache.put(null, "");
                TestCase.fail("Shouldn't have gotten here");
                
            }
            catch(Exception e) {
                //expected behavior
            }
            
            try {
                
                cache.put("", null);
                TestCase.fail("Shouldn't have gotten here");
                
            }
            catch(Exception e) {
                //expected behavior
            }   
            
            try {
                
                cache.remove(null);
                TestCase.fail("Shouldn't have gotten here");
                
            }
            catch(Exception e) {
                //expected behavior
            }
            
        }
        
    }
    
    @Test
    public void testNegConstruction() {
            
        {
           
            try {
                
                new ExpiringCache<String, String>(null, 10000, 10000);
                TestCase.fail("Shouldn't have gotten here");
                
            }
            catch(Exception e) {
                //expected behavior
            }
            
            try {
                
                new ExpiringCache<String, String>(new MemoryCache<String, String>(), 0, 10000);
                TestCase.fail("Shouldn't have gotten here");
                
            }
            catch(Exception e) {
                //expected behavior
            }
            
            try {
                
                new ExpiringCache<String, String>(new MemoryCache<String, String>(), 10000, 0);
                TestCase.fail("Shouldn't have gotten here");
                
            }
            catch(Exception e) {
                //expected behavior
            }
            
        }
        
    }

}
