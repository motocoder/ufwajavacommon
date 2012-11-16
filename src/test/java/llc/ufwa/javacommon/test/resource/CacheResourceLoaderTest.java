package llc.ufwa.javacommon.test.resource;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.MemoryCache;
import llc.ufwa.data.resource.loader.CachedResourceLoader;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.data.resource.loader.ResourceLoader;

import org.junit.Test;

public class CacheResourceLoaderTest {
    
    private enum ReturnType {EXCEPTION, KEY, NULL};
    
    @Test
    public void cacheResourceLoaderTest() {
        
        final ParallelControl<ReturnType> returnType = new ParallelControl<ReturnType>();
        
        returnType.setValue(ReturnType.KEY);
        
        final ResourceLoader<String, String> root = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {

                if(key.equals("ignore1") || key.equals("ignore2")) {
                    return null;
                }
                
                final String returnVal;
                
                switch(returnType.getValue()) {
                
                    case EXCEPTION:
                    {
                        throw new TestException();
                    }
                    case NULL:
                    {
                        returnVal = null;
                        break;
                    }
                    case KEY: 
                    {
                        returnVal = key;
                        break;
                    }
                    default:
                        throw new ResourceException("WTF");
                        
                }
                
                return returnVal;
                
            }
            
        };
        
        final Cache<String, String> valueCache = new MemoryCache<String, String>();
        final Cache<String, Boolean> searchCache = new MemoryCache<String, Boolean>();
        
        final ResourceLoader<String, String> cached = new CachedResourceLoader<String, String>(root, valueCache, searchCache);
        
        try {
            
            {   
                
                TestCase.assertEquals("key", cached.get("key"));
                
                returnType.setValue(ReturnType.EXCEPTION);
                
                TestCase.assertEquals("key", cached.get("key"));
                TestCase.assertEquals(true, cached.exists("key"));
                
                try {
                    
                    cached.get("key2");
                    TestCase.fail("Should not get here");
                    
                }
                catch(TestException e) {
                    //expected behavior
                }
            }
            
            valueCache.clear();
            searchCache.clear();
            
            returnType.setValue(ReturnType.KEY);
            
            {
                
                TestCase.assertEquals(true, cached.exists("key"));
                
                TestCase.assertEquals("key", cached.get("key"));
                
                returnType.setValue(ReturnType.EXCEPTION);
                
                TestCase.assertEquals("key", cached.get("key"));
                TestCase.assertEquals(true, cached.exists("key"));
                
                try {
                    
                    cached.get("key2");
                    TestCase.fail("Should not get here");
                    
                }
                catch(TestException e) {
                    //expected behavior
                }
                
            }
            
            valueCache.clear();
            searchCache.clear();
            
            returnType.setValue(ReturnType.KEY);
            
            {
                
                cached.get("key");
                
                returnType.setValue(ReturnType.NULL);
                
                cached.get("key2");
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("key");
                keys.add("ignore1");                
                keys.add("key2");
                keys.add("ignore2");
                
                List<String> returned = cached.getAll(keys);
                
                TestCase.assertEquals("key", returned.get(0));
                TestCase.assertEquals(null, returned.get(1));
                TestCase.assertEquals(null, returned.get(2));
                TestCase.assertEquals(null, returned.get(3));
                
            }
            
        }
        catch(ResourceException exception) {
            
            exception.printStackTrace();
            TestCase.fail("Should not get here");
            
        }
        
    }

    private static class TestException extends ResourceException {
        
    }
}
