package llc.ufwa.javacommon.test.resource;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.LimitingExecutorService;
import llc.ufwa.concurrency.LimitingExecutorServiceFactory;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.data.resource.loader.DoubleHitParallelResourceLoader;
import llc.ufwa.data.resource.loader.ParallelResourceLoaderImpl;
import llc.ufwa.data.resource.loader.ResourceEvent;
import llc.ufwa.data.resource.loader.ResourceLoader;

import org.junit.Test;

public class DoubleHitResourceLoaderTest {
    
    @Test
    public void testDoubleHitResourceLoader() {
        
        final ExecutorService bulk = Executors.newFixedThreadPool(50);
        
        final LimitingExecutorService limited = LimitingExecutorServiceFactory.createExecutorService(
                bulk, 
                bulk,10);
        
        final LimitingExecutorService limited2 = LimitingExecutorServiceFactory.createExecutorService(
                bulk, 
                bulk,10);
                
        final ResourceLoader<String, String> primaryRoot = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                return "primary " + key;
            }
            
        };
        
        final ResourceLoader<String, String> secondaryRoot = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                return "secondary " + key;
            }
            
        };
        
        final ParallelResourceLoaderImpl<String, String> primaryLoader =
            new ParallelResourceLoaderImpl<String, String>(primaryRoot, limited, bulk, 10, "primary");
        
        final ParallelResourceLoaderImpl<String, String> secondaryLoader =
            new ParallelResourceLoaderImpl<String, String>(secondaryRoot, limited2, bulk, 10, "secondary");
        
        final DoubleHitParallelResourceLoader<String, String> doubleHit = 
            new DoubleHitParallelResourceLoader<String, String>(primaryLoader, secondaryLoader);
        
        try {
            
            TestCase.assertTrue(doubleHit.exists("1"));
            
            TestCase.assertEquals("secondary 1", doubleHit.get("1"));
            
            {
            
                final ParallelControl<Boolean> control1 = new ParallelControl<Boolean>();
                
                doubleHit.getParallel(
                        
                    new Callback<Object, ResourceEvent<String>>() {
    
                        private boolean loadedPrimary;
                        private boolean loadedSecondary;
                        
                        @Override
                        public Object call(ResourceEvent<String> value) {
                        
                            if(!loadedSecondary) {
                             
                                if("primary 1".equals(value.getVal())) {
                                    loadedPrimary = true;
                                }
                                else if("secondary 1".equals(value.getVal())) {
                                    
                                    if(loadedPrimary) {
                                        control1.setValue(true);
                                    }
                                    
                                    control1.unBlockOnce();
                                    
                                }
                                
                            }
                            
                            return null;
                            
                        }
                        
                    },
                    "1"
                );
                
                //Test to see if primary loads, then secondary
                control1.blockOnce();
                
                TestCase.assertTrue(control1.getValue());
            
            }
            
            //TODO test getAll
            
        }
        catch (ResourceException e) {
            
            e.printStackTrace();
            
            TestCase.fail();
            
        }
        catch (InterruptedException e) {
            
            e.printStackTrace();
            
            TestCase.fail();
            
        }
        
    }

}
