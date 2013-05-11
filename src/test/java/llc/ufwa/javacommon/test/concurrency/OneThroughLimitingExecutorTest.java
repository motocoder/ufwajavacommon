package llc.ufwa.javacommon.test.concurrency;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;
import junit.framework.TestCase;

import llc.ufwa.concurrency.LimitingExecutorService;
import llc.ufwa.concurrency.LimitingExecutorServiceFactory;
import llc.ufwa.concurrency.OneThroughLimitingExecutor;
import llc.ufwa.concurrency.ParallelControl;

import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneThroughLimitingExecutorTest {
	
	static {
        BasicConfigurator.configure();
    }
	
	private static final Logger logger = LoggerFactory.getLogger(OneThroughLimitingExecutorTest.class);	
	
	@Test
	public void testOneThroughLimitingExec() {
	    
	    final LimitingExecutorService service = 
            LimitingExecutorServiceFactory.createExecutorService(
                Executors.newFixedThreadPool(1),
                Executors.newFixedThreadPool(1),
                1
            );
		
        OneThroughLimitingExecutor oneThrough = new OneThroughLimitingExecutor(Executors.newFixedThreadPool(1));
        
        final ParallelControl<String> control1 = new ParallelControl<String>();
        final ParallelControl<String> control2 = new ParallelControl<String>();
        final ParallelControl<String> control3 = new ParallelControl<String>();
        
        control1.setValue(null);
        
        oneThrough.execute(
        		
        		new Runnable() {
            
        			@Override
        			public void run() {
        			    
        				try {
        				    
        					logger.debug("first runnable before sleep");
        					control1.blockOnce();
        					
        				}
        				catch(Exception e){
        					e.printStackTrace();
        				}
        				
        			}
        			
        		}
        		
        );
        
        try {
			Thread.sleep(300);
		}
        catch(InterruptedException e2) {
			e2.printStackTrace();
		}
        
        oneThrough.execute(
        		
    		new Runnable() {
        
    			@Override
    			public void run() {
    			    
    				try {
    				    
    					logger.debug("second runnable");
    					
    					control1.setValue("1");
    					control2.unBlockOnce();
    					
    				}
    				catch(Exception e){
    					e.printStackTrace();
    				}
    				
    			}
    			
    		}
    		
        );
        
        oneThrough.execute(
                
            new Runnable() {
        
                @Override
                public void run() {
                    
                    try {
                        
                        logger.debug("second runnable");
                        control2.setValue("2");
                        control2.unBlockOnce();
                        
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    
                }
                
            }
            
        );
        
        oneThrough.execute(
                
            new Runnable() {
        
                @Override
                public void run() {
                    
                    try{
                        
                        logger.debug("second runnable");
                        control3.setValue("3");
                        control2.unBlockOnce();
                        
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    
                }
                
            }
            
        );
        
        
        try {
			Thread.sleep(500);
		} 
        catch(InterruptedException e1) {
			e1.printStackTrace();
		}
        
        TestCase.assertNull(control1.getValue());
                
        control1.unBlockOnce();
        
        try {
            control2.blockOnce();
        } 
        catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        
        TestCase.assertEquals(null, control1.getValue());
        TestCase.assertEquals(null, control2.getValue());
        TestCase.assertEquals("3", control3.getValue());
        
        oneThrough.execute(
        		
        		new Runnable() {
            
        			@Override
        			public void run() {
        				
        			    try{
        				    
        					logger.debug("third runnable");
        					control1.setValue("");
        					control1.unBlockOnce();
        					
        				}
        				catch(Exception e){
        					e.printStackTrace();
        				}
        			    
        			}
        			
        		}
        		
        );
        
        try {
			control1.blockOnce();
		}
        catch(InterruptedException e) {
			e.printStackTrace();
		}
        
        TestCase.assertNotNull(control1.getValue());
        
        
	}

		
}
