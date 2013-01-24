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
		
		LimitingExecutorService service = new LimitingExecutorService(Executors.newFixedThreadPool(1), Executors.newFixedThreadPool(1), 1) {

            @Override
            public <T> List<Future<T>> invokeAll(
                    Collection<? extends Callable<T>> tasks)
                    throws InterruptedException {
                throw new RuntimeException("NOT SUPPORTED");
            }

            @Override
            public <T> List<Future<T>> invokeAll(
                    Collection<? extends Callable<T>> tasks, long timeout,
                    TimeUnit unit) throws InterruptedException {
                throw new RuntimeException("NOT SUPPORTED");
            }

            @Override
            public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
                    throws InterruptedException, ExecutionException {
                throw new RuntimeException("NOT SUPPORTED"); 
            }

            @Override
            public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit) throws InterruptedException,
                    ExecutionException, TimeoutException {
                throw new RuntimeException("NOT SUPPORTED");
            }
        };
        
        OneThroughLimitingExecutor oneThrough = new OneThroughLimitingExecutor(Executors.newFixedThreadPool(1), service);
        
        final ParallelControl<String> control1 = new ParallelControl<String>();
        
        control1.setValue(null);
        
        oneThrough.execute(
        		
        		new Runnable() {
            
        			@Override
        			public void run() {
        				try{
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
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
        
        oneThrough.execute(
        		
        		new Runnable() {
            
        			@Override
        			public void run() {
        				try{
        					logger.debug("second runnable");
        					control1.setValue("");
        				}
        				catch(Exception e){
        					e.printStackTrace();
        				}
        			}
        		}
        );
        
        
        try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        
        if(control1.getValue() != null) {
        	Assert.fail();
        }
        
        control1.unBlockOnce();
        
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        if(control1.getValue() == null) {
        	TestCase.fail();
        }
        
	}

		
}
