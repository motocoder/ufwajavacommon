package llc.ufwa.javacommon.test.cache;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.SynchronizedCache;

import org.junit.Test;

public class SynchronizedCacheTest {
	
	@Test
    public void test() { 
		
		final Cache<String, String> cache = 
				new SynchronizedCache<String, String>(
					new Cache<String, String>() {

						@Override
						public String get(String key) throws ResourceException {
			                return key;
						}

						@Override
						public boolean exists(String key) throws ResourceException {
							return false;
						}

						@Override
						public List<String> getAll(List<String> keys) throws ResourceException {
							return null;
						}

						@Override
						public void clear() {
						}

						@Override
						public void remove(String key) {
						}

						@Override
						public void put(String key, String value) {
						}
						
					}
					
				);
		
    	try {
    		
			TestCase.assertEquals(cache.get("key"), "key");
			TestCase.assertEquals(false, cache.exists("key"));
            
			cache.put("key", "test");
            
            TestCase.assertEquals(cache.get("key"), "key");
            TestCase.assertEquals(false, cache.exists("key"));
            
            cache.remove("key");
            
            TestCase.assertEquals(cache.get("key"), "key");
            TestCase.assertEquals(false, cache.exists("key"));
            
            cache.clear();
            
            TestCase.assertEquals(cache.get("key"), "key");
            TestCase.assertEquals(false, cache.exists("key"));
            
            {
            	
	        	final List<String> keys = new ArrayList<String>();
	            
	            keys.add("test1");
	            keys.add("test2");
	            
	            final List<String> results = cache.getAll(keys);
	            
	            TestCase.assertEquals(cache.get("key"), "key");
	            TestCase.assertEquals(false, cache.exists("key"));
            
            }
            
    	}
    	
        catch(ResourceException e) {
            TestCase.fail("failed");
        }
        
    }
	
	@Test
	public void test2() throws InterruptedException {
		
		final ParallelControl<String> control1 = new ParallelControl<String>();
		final ParallelControl<String> control2 = new ParallelControl<String>();
			
		final Cache<String, String> cache = 
				new SynchronizedCache<String, String>(
					new Cache<String, String>() {

						@Override
						public String get(String key) throws ResourceException {
			                
						    try {
			                	
			                	control1.unBlockOnce();
								control2.blockOnce();
								
							} 
						    catch (InterruptedException e) {
								e.printStackTrace();
							}
			                
							return key;
						
						}

						@Override
						public boolean exists(String key) throws ResourceException {
							return false;
						}

						@Override
						public List<String> getAll(List<String> keys) throws ResourceException {
							return null;
						}

						@Override
						public void clear() {
						}

						@Override
						public void remove(String key) {
						}

						@Override
						public void put(String key, String value) {
						}
						
					} 
					
				);
		
		Runnable thread1 = new Runnable() {
			
		    public void run() {
				
		        try {
					
					cache.put("key", "test");
					
					TestCase.assertEquals(cache.get("key"), "key");
					control2.setValue("");
					TestCase.assertEquals(false, cache.exists("key"));
					
					cache.put("key", "test");
					
					control2.unBlockOnce();
					
				}
				catch(ResourceException e) {
		            TestCase.fail("failed");
		        }
				
			}
			
		};
		
		Runnable thread2 = new Runnable() {
			
		    public void run() {
				
		        try {
					
					control1.unBlockOnce();
					TestCase.assertEquals(cache.get("key"), "key");
					TestCase.assertEquals(false, cache.exists("key"));
	            
					cache.put("key", "test");
					
					control1.unBlockOnce();
					
				}
				catch(ResourceException e) {
		            TestCase.fail("failed");
		        }
				
			}
			
		};
		
		new Thread(thread1).start();
		new Thread(thread2).start();
		
		TestCase.assertNull(control2.getValue());
        
        control2.unBlockOnce();
        
        Thread.yield();
        Thread.sleep(30);
        
        TestCase.assertNotNull(control2.getValue()); //might fail if thread.sleep doesn't work properly
		
	}
	
}
