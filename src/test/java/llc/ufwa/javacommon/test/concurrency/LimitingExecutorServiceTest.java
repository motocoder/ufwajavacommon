package llc.ufwa.javacommon.test.concurrency;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;
import llc.ufwa.concurrency.LimitingExecutorService;
import llc.ufwa.concurrency.LimitingExecutorServiceFactory;

import org.junit.Test;

public class LimitingExecutorServiceTest {

    private final Object lock = new Object();
    
    private int current = 0;
    private int max = 0;
    
    @Test 
    public void testLimitingExecutor() {
        
        final LimitingExecutorService service = 
            LimitingExecutorServiceFactory.createExecutorService(
                Executors.newFixedThreadPool(100),
                Executors.newFixedThreadPool(100),
                5
            );
        
        for(int i = 0; i < 1000; i++) {
            
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    
                    synchronized(lock) { 
                        
                        current++;
                        
                        if(current > max) {
                            max = current;
                        }
                        
                        try {
                            
                            try {
                                lock.wait();
                            } 
                            catch (InterruptedException e) {
                            }
                            
                        } 
                        finally {
                            
                            synchronized(lock) { 
                                current--;
                            }
                            
                        }
                        
                    }
                    
                }
                
            };
            
//            System.out.println("test runnable " + finalI + " " + runnable);
             
            service.execute(                
                runnable
            );
            
//            try {
//                Thread.sleep(1);
//            } 
//            catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            
        }
        
        try {
            Thread.sleep(3000);
        } 
        catch (InterruptedException e1) {
            TestCase.fail();
        }
        
        
        TestCase.assertEquals(5, max);
        TestCase.assertEquals(5, current);
        
        synchronized(lock) {
            lock.notifyAll();
        }
        
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestCase.assertEquals(0, current);
        
    }
    
}
