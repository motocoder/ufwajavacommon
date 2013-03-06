package llc.ufwa.javacommon.test.resource;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.ParallelControl;
import llc.ufwa.data.exception.OutOfRetriesException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.data.resource.loader.ResourceLoader;
import llc.ufwa.data.resource.loader.RetryingOnExceptionResourceLoader;
import llc.ufwa.data.resource.loader.RetryingOnNullResourceLoader;

import org.junit.Test;

public class TestRetryingResourceLoader {
    
    private enum ReturnType {EXCEPTION, NULL, KEY};

    @Test
    public void testRetryingResourceLoader() {
        
        final ParallelControl<ReturnType> loaderControl = new ParallelControl<ReturnType>();
        final ParallelControl<String> exceptionBlock = new ParallelControl<String>();
        
        loaderControl.setValue(ReturnType.KEY);
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                
                final String returnVal;
                
                switch(loaderControl.getValue()) {
                    case NULL:
                    {
                        returnVal = null;
                        break;
                    }
                    case EXCEPTION:
                    {
                        
                        try {
                            exceptionBlock.blockOnce();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        throw new TestException();
                    }
                    case KEY:
                    {
                        returnVal = key;
                        break;
                    }
                    default:
                        throw new RuntimeException("um wtf");
                }
                
                return returnVal;
                
            }
            
        };
        
        final Callback<Void, String> onRetry = new Callback<Void, String>() {

            @Override
            public Void call(
                final String value
            ) {
                return null;
            }
            
        };
        
        final Set<Class<? extends ResourceException>> exceptionsHandled = new HashSet<Class<? extends ResourceException>>();
        exceptionsHandled.add(TestException.class);
        
        final RetryingOnExceptionResourceLoader<String, String> loader = 
            new RetryingOnExceptionResourceLoader<String, String>(
                internal,
                onRetry, 
                2,
                exceptionsHandled
            );
        
        try {
            
            TestCase.assertEquals("key", loader.get("key"));
            loaderControl.setValue(ReturnType.EXCEPTION);
            
            try {
            
                exceptionBlock.unBlockOnce();
                exceptionBlock.unBlockOnce();
                loader.get("key");
                TestCase.fail("Should have thrown exception");
                
            }
            catch(OutOfRetriesException e) {
                //expected behavior
            }
            
            try {
                
                exceptionBlock.unBlockOnce();
                loaderControl.setValue(ReturnType.KEY);
                exceptionBlock.unBlockOnce();
                TestCase.assertEquals("key", loader.get("key"));
                
                
            }
            catch(OutOfRetriesException e) {
                TestCase.fail("Should have thrown exception");
            }
            
        }
        catch (ResourceException e) {
            
            e.printStackTrace();
            TestCase.fail("Should not get here");
            
        }
        
        
    }
    
    @Test
    public void testNullRetryingResourceLoader() {
        
        final ParallelControl<ReturnType> loaderControl = new ParallelControl<ReturnType>();
        final ParallelControl<String> exceptionBlock = new ParallelControl<String>();
        
        loaderControl.setValue(ReturnType.KEY);
        
        final ResourceLoader<String, String> internal = new DefaultResourceLoader<String, String>() {

            @Override
            public String get(String key) throws ResourceException {
                
                final String returnVal;
                
                switch(loaderControl.getValue()) {
                    case NULL:
                    {
                        returnVal = null;
                        break;
                    }
                    case EXCEPTION:
                    {
                        
                        try {
                            exceptionBlock.blockOnce();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        throw new TestException();
                    }
                    case KEY:
                    {
                        returnVal = key;
                        break;
                    }
                    default:
                        throw new RuntimeException("um wtf");
                }
                
                return returnVal;
                
            }
            
        };
        
        final Callback<Void, String> onRetry = new Callback<Void, String>() {

            @Override
            public Void call(
                final String value
            ) {
                return null;
            }
            
        };
        
        final RetryingOnNullResourceLoader<String, String> loader = 
            new RetryingOnNullResourceLoader<String, String>(
                internal,
                onRetry, 
                2
            );
        
        try {
            
            TestCase.assertEquals("key", loader.get("key"));
            loaderControl.setValue(ReturnType.NULL);
            
            exceptionBlock.unBlockOnce();
            exceptionBlock.unBlockOnce();
            TestCase.assertNull(loader.get("key"));
                
            exceptionBlock.unBlockOnce();
            loaderControl.setValue(ReturnType.KEY);
            exceptionBlock.unBlockOnce();
            TestCase.assertEquals("key", loader.get("key"));
            
        }
        catch (ResourceException e) {
            
            e.printStackTrace();
            TestCase.fail("Should not get here");
            
        }
        
    }
    
    private class TestException extends ResourceException {

        private static final long serialVersionUID = 5818948637592911952L;
        
    }
    
}
