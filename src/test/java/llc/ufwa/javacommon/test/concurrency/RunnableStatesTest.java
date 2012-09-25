package llc.ufwa.javacommon.test.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import llc.ufwa.concurrency.RunnableStates;
import llc.ufwa.concurrency.RunnableStates.SequencedRunnable;
import llc.ufwa.data.resource.provider.ResourceProvider;

import org.junit.Test;

public class RunnableStatesTest {
    
    
//
//    @Test
//    public void testRunnableStates() {
//    
//        final Executor runner = Executors.newFixedThreadPool(100);
//        
//        final int LIMIT = 10;
//        
//        final RunnableStates states = 
//            new RunnableStates(
//                Executors.newFixedThreadPool(100),
//                LIMIT,
//                new ResourceProvider<Integer>() {
//
//                    private int i;
//                    
//                    @Override
//                    public boolean exists() {
//                        return true;
//                    }
//        
//                    @Override
//                    public synchronized Integer provide() {
//                        return i++;
//                    }
//                    
//                }
//                
//            );
//        
//        final ExecutorService submitOn = Executors.newFixedThreadPool(100);
//        
//        runner.execute(
//            new Runnable() {
//
//                @Override
//                public void run() {
//                    
//                    int i = 0;
//                    
//                    while(true) {
//                        try {
//                            
//                            final SequencedRunnable next = states.getNext();
//     
//                            final Future<Void> future = submitOn.submit(
//                                new Callable<Void>() {
//    
//                                    @Override
//                                    public Void call() throws Exception {
//                                        
//                                        try {
//                                            if(states.started(next)) {
//                                                next.getRunnable().run();      
//                                            }
//                                            
//                                        }
//                                        finally {
//                                            states.finish(next);
//                                        }
//                                        return null;
//                                    }
//                                }
//                            );
//                            
//                            if(i % 2 == 0) {
//                                Thread.sleep(100);
//                            }
//                            
//                            states.launched(future, next);
//                             
//                            i++;
//                            
//                        }
//                        catch(Exception e) {
//                            e.printStackTrace();
//                        }
//                        
//                    }
//                    
//                }
//                
//            }
//           
//        );
//        
//        final int COUNT = 1000;
//        
//        for(int i = 0; i < COUNT; i++) {
//            
//            final int finalI = i;
//            
//            states.schedule(
//                new Runnable() {
//
//                    @Override
//                    public void run() {
//                        System.out.println("Running2 " + finalI);    
//                        
//                        try {
//                			Thread.sleep(10000);
//                		}
//                        catch (InterruptedException e) {
//                			System.out.println("Interrupted " + finalI);
//                		}
//                    }
//                    
//                }
//                
//            );
//            
//        }
//        
//        try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        
//    }
}
