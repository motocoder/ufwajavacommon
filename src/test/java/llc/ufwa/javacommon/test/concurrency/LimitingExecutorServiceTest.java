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

import org.junit.Test;

public class LimitingExecutorServiceTest {

    private final Object lock = new Object();
    private int total = 0;
    private int current = 0;
    private int max = 0;
    
    @Test 
    public void testLimitingExecutor() {
        
        final LimitingExecutorService service = new LimitingExecutorService(Executors.newFixedThreadPool(100), Executors.newFixedThreadPool(100), 5) {

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
        
        for(int i = 0; i < 1000; i++) {
    
            final int finalI = i;
            
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    
                    synchronized(lock) { 
                        
                        total++;
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
        
        System.out.println("current " + current);
        System.out.println("max " + max);
        
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
