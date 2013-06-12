package llc.ufwa.javacommon.test.concurrency;

import java.util.concurrent.Executors;

import junit.framework.TestCase;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.Debouncer;
import llc.ufwa.concurrency.ParallelControl;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebouncerTest {

	private static final Logger logger = LoggerFactory.getLogger(DebouncerTest.class);
	
    @Test 
    public void testDebouncer() {
        
    	for (int x = 0; x < 10; x++) {
	    	
	        final ParallelControl<Integer> control = new ParallelControl<Integer>();
	        
	        final Debouncer debouncer = new Debouncer(new Callback<Object, Object>() {
	
	            @Override
	            public Object call(Object value) {
	                
	                Integer val = control.getValue();
	                
	                if(val == null) {
	                    val = 1;
	                }
	                else {
	                    val = val + 1;
	                }
	                
	                control.setValue(val);
	                
	                logger.debug("set control value to " + val);
	                
	                return null;
	                
	            }}, Executors.newFixedThreadPool(10), 1000);
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        debouncer.signal();
	        debouncer.signal();
	        debouncer.signal();
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }

	        TestCase.assertEquals(1, (int)control.getValue());
	        
	        debouncer.signal();
	        debouncer.signal();
	        debouncer.signal();
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }

	        TestCase.assertEquals(2, (int)control.getValue());
	        
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(3, (int)control.getValue());

	        if ((int)control.getValue() != 3) {
                logger.debug("control value should be 3, but is " + (int)control.getValue());
	        }
	        
    	}
        
    }
    
    @Test 
    public void testDebouncerZeroTime() {

    	for (int x = 0; x < 20; x++) {
    		
	        final ParallelControl<Integer> control = new ParallelControl<Integer>();
	        
	        final Debouncer debouncer = new Debouncer(new Callback<Object, Object>() {
	
	            @Override
	            public Object call(Object value) {
	                
	                try {
	                    Thread.sleep(50);
	                } 
	                catch (InterruptedException e) {
	                }
	                
	                Integer val = control.getValue();
	                
	                if(val == null) {
	                    val = 1;
	                }
	                else {
	                    val = val + 1;
	                }
	                
	                control.setValue(val);
	                
	                return null;
	                
	            }}, Executors.newFixedThreadPool(10), 1);
	        
	        debouncer.signal();
	        debouncer.signal();
	        debouncer.signal();
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(500);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        debouncer.signal();
	        debouncer.signal();
	        debouncer.signal();
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(500);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(500);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals((int)control.getValue(), 3);
	        
    	}
        
    }
}
