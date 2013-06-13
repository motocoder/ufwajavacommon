package llc.ufwa.javacommon.test.concurrency;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import junit.framework.TestCase;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.Debouncer;
import llc.ufwa.concurrency.ExecutorServiceDebouncer;
import llc.ufwa.concurrency.ParallelControl;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebouncerTest {

	private static final Logger logger = LoggerFactory.getLogger(DebouncerTest.class);
	
    //@Test 
    public void testDebouncer() {
        
    	for (int x = 0; x < 5; x++) {
	    	
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
    
    //@Test 
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
    
    @Test 
    public void testExecutorServiceDebouncerZeroTime() {

    	for (int x = 0; x < 20; x++) {
    		
	        final ParallelControl<Integer> control = new ParallelControl<Integer>();
	        	        
	        final ExecutorServiceDebouncer debouncer = new ExecutorServiceDebouncer(new Callback<Object, Object>() {
	
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
	                
	            }}, new ScheduledThreadPoolExecutor(10), 1000, ExecutorServiceDebouncer.RunType.RUN_AFTER);
	        
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
	        
	        TestCase.assertNotSame((int)control.getValue(), 3); //this should be not the same because every debouncer.signal() call should run, 
	        													//since there is no delay between them
	        
    	}
        
    }
    
    @Test 
    public void testExecutorServiceDebouncerMultiThreaded() {
        
	    Runnable r = new Runnable() {
	    	
	        public void run() {
	            
	        	testExecutorServiceDebouncerRunAfter();
	        	
	        }
	    };
		
	    for (int x = 0; x < 25; x++) {
	    	new Thread(r).start();
	    }
    	
    }
    
    
    @Test 
    public void testExecutorServiceDebouncerRunAfter() {
        
    	for (int x = 1; x < 5; x++) {
    		
	        final ParallelControl<Integer> control = new ParallelControl<Integer>();
	        
	        final ExecutorServiceDebouncer debouncer = new ExecutorServiceDebouncer(new Callback<Object, Object>() {
	
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
	                
	            }}, new ScheduledThreadPoolExecutor(10), 700, ExecutorServiceDebouncer.RunType.RUN_AFTER);
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }

	        for (int x1 = 0; x1 < 20; x1++) {
	        	debouncer.signal();
	        }
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }

	        TestCase.assertEquals(1, (int)control.getValue());
	        
	        for (int x1 = 0; x1 < 10000; x1++) {
	        	debouncer.signal();
	        }
	        
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
	        
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(4, (int)control.getValue());
	        
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(5, (int)control.getValue());
	        
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(6, (int)control.getValue());
	        
	        try {
	            Thread.sleep(3000);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(6, (int)control.getValue());
	        
	        debouncer.shutdownNow();
	        
	        try {
	            Thread.sleep(3000);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(6, (int)control.getValue());
	        
    	}
        
    }
    
    @Test 
    public void testExecutorServiceDebouncerRunBefore() {
        
    	for (int x = 1; x < 5; x++) {
	    	
	        final ParallelControl<Integer> control = new ParallelControl<Integer>();
	        
	        final ExecutorServiceDebouncer debouncer = new ExecutorServiceDebouncer(new Callback<Object, Object>() {
	
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
	                
	            }}, new ScheduledThreadPoolExecutor(10), 700, ExecutorServiceDebouncer.RunType.RUN_BEFORE);
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }

	        for (int x1 = 0; x1 < 20; x1++) {
	        	debouncer.signal();
	        }
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }

	        TestCase.assertEquals(1, (int)control.getValue());
	        
	        for (int x1 = 0; x1 < 10000; x1++) {
	        	debouncer.signal();
	        }
	        
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
	        
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(4, (int)control.getValue());
	        
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(5, (int)control.getValue());
	        
	        debouncer.signal();
	        
	        try {
	            Thread.sleep(1100);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(6, (int)control.getValue());
	        
	        try {
	            Thread.sleep(3000);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(6, (int)control.getValue());
	        
	        debouncer.shutdownNow();
	        
	        try {
	            Thread.sleep(3000);
	        } 
	        catch (InterruptedException e) {
	            TestCase.fail();
	        }
	        
	        TestCase.assertEquals(6, (int)control.getValue());
	        
    	}
        
    }
    
}
