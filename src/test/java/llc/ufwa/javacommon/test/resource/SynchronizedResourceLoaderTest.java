
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
import llc.ufwa.data.resource.loader.SynchronizedResourceLoader;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SynchronizedResourceLoaderTest {
    private enum ReturnType {EXCEPTION, KEY, NULL};
    
    static {
        BasicConfigurator.configure();
    }
    
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

    @Test
    public void synchronizedResourceLoaderTest() {
        
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
        
        final ResourceLoader<String, String> synched = new SynchronizedResourceLoader<String, String>(cached);
        
        try {
            
            {   
                
                TestCase.assertEquals("key", synched.get("key"));
                
                returnType.setValue(ReturnType.EXCEPTION);
                
                TestCase.assertEquals("key", synched.get("key"));
                TestCase.assertEquals(true, synched.exists("key"));
                
                try {
                    
                	synched.get("key2");
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
                
                TestCase.assertEquals(true, synched.exists("key"));
                
                TestCase.assertEquals("key", synched.get("key"));
                
                returnType.setValue(ReturnType.EXCEPTION);
                
                TestCase.assertEquals("key", synched.get("key"));
                TestCase.assertEquals(true, synched.exists("key"));
                
                try {
                    
                    synched.get("key2");
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
                
                synched.get("key");
                
                returnType.setValue(ReturnType.NULL);
                
                synched.get("key2");
                
                final List<String> keys = new ArrayList<String>();
                
                keys.add("key");
                keys.add("ignore1");                
                keys.add("key2");
                keys.add("ignore2");
                
                List<String> returned = synched.getAll(keys);
                
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
		
    	private static final long serialVersionUID = 1L;
       
    }
}
