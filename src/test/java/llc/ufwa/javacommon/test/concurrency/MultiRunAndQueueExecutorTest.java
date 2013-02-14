//package llc.ufwa.javacommon.test.concurrency;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//import junit.framework.TestCase;
//import llc.ufwa.concurrency.LimitingExecutorService;
//import llc.ufwa.concurrency.MultiRunAndQueueExecutor;
//import llc.ufwa.concurrency.ParallelControl;
//
//import org.apache.log4j.BasicConfigurator;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class MultiRunAndQueueExecutorTest {
//    
//    static {
//        BasicConfigurator.configure();
//    }
//    
//    private static final Logger logger = LoggerFactory.getLogger(OneThroughLimitingExecutorTest.class); 
//    
//    @Test
//    public void MultiRunAndQueueExecutorTest() {
//        
//        //TODO Ok so now that you understand the dynamics of your test case,
//        //I want you to modify the test case so that it also stress tests your class,
//        //Basically what this will do is run a whole shit ton of runnables in your executor,
//        //and test to make sure only the right amount are ever running at one time,
//        //it should also test to make sure that amount and not less than that amount is running,
//        // if they have been added to the queue.
//        //Currently your test case only tests running once through, in the real world, thousands of threads will
//        // be running through this
//        
//        MultiRunAndQueueExecutor multiRun = new MultiRunAndQueueExecutor(Executors.newFixedThreadPool(20), 4, 3);
//        
//        final ParallelControl<String> control1 = new ParallelControl<String>();
//        final ParallelControl<String> control2 = new ParallelControl<String>();
//        final ParallelControl<String> control3 = new ParallelControl<String>();
//        final ParallelControl<String> control4 = new ParallelControl<String>();
//        
//        control1.setValue(null);
//        control2.setValue(null);
//        control3.setValue(null);
//        control4.setValue(null);
//        
//        multiRun.execute(
//                
//                new Runnable() {
//            
//                    @Override
//                    public void run() {
//                        
//                        try {
//                            
//                            logger.debug("first runnable");
//                            control1.setValue("1");
//                            control1.blockOnce();
//                            
//                            
//                        }
//                        catch(Exception e){
//                            e.printStackTrace();
//                        }
//                        
//                    }
//                    
//                }
//                
//        );
//        
//  
//        
//        multiRun.execute(
//                
//            new Runnable() {
//        
//                @Override
//                public void run() {
//                    
//                    try {
//                        
//                        logger.debug("second runnable");
//                        control2.setValue("2");
//                        control2.blockOnce();
//                        
//                        
//                    }
//                    catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    
//                }
//                
//            }
//            
//        );
//        
//        multiRun.execute(
//                
//            new Runnable() {
//        
//                @Override
//                public void run() {
//                    
//                    try {
//                        
//                        logger.debug("third runnable");
//                        control3.setValue("3");
//                        control3.blockOnce();
//                       
//                                           
//                    }
//                    catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    
//                }
//                
//            }
//            
//        );
//        
//        multiRun.execute(
//                
//            new Runnable() {
//        
//                @Override
//                public void run() {
//                    
//                    try{
//                        
//                        logger.debug("fourth runnable");
//                        control4.setValue("4");
//                        control4.blockOnce();
//                     
//                        
//                    }
//                    catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    
//                }
//                
//            }
//            
//        );
//        
//        
//        try {
//            Thread.sleep(500);
//        } 
//        catch(InterruptedException e1) {
//            e1.printStackTrace();
//        }
//     
//                
//        
//        TestCase.assertEquals("1", control1.getValue());
//        TestCase.assertEquals("2", control2.getValue());
//        TestCase.assertEquals("3", control3.getValue());
//        TestCase.assertEquals("4", control4.getValue());
//        
//
//        
//        multiRun.execute(
//                
//                new Runnable() {
//            
//                    @Override
//                    public void run() {
//                        
//                        try {
//                            
//                            logger.debug("first queueable");
//                            control1.setValue("11");
//
//                            
//                        }
//                        catch(Exception e){
//                            e.printStackTrace();
//                        }
//                        
//                    }
//                    
//                }
//                
//        );
//        
//  
//        
//        multiRun.execute(
//                
//            new Runnable() {
//        
//                @Override
//                public void run() {
//                    
//                    try {
//                        
//                        logger.debug("second queueable");
//                        control2.setValue("22");
//                        
//                    }
//                    catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    
//                }
//                
//            }
//            
//        );
//        
//        multiRun.execute(
//                
//            new Runnable() {
//        
//                @Override
//                public void run() {
//                    
//                    try {
//                        
//                        logger.debug("third queueable");
//                        control3.setValue("33");
//                        
//                    }
//                    catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    
//                }
//                
//            }
//            
//        );
//        
//        multiRun.execute(
//                
//            new Runnable() {
//        
//                @Override
//                public void run() {
//                    
//                    try{
//                        
//                        logger.debug("fourth queueable");
//                        control4.setValue("44");
//                        
//                    }
//                    catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    
//                }
//                
//            }
//            
//        );
//        
//        try {
//            Thread.sleep(500);
//        } 
//        catch(InterruptedException e1) {
//            e1.printStackTrace();
//        }
//        
//        TestCase.assertEquals("1", control1.getValue());
//        TestCase.assertEquals("2", control2.getValue());
//        TestCase.assertEquals("3", control3.getValue());
//        TestCase.assertEquals("4", control4.getValue());
//        
//       
//        
//        control1.unBlockOnce();
//        control2.unBlockOnce();
//        control3.unBlockOnce();
//        control4.unBlockOnce();
//        
//        try {
//            Thread.sleep(500);
//        } 
//        catch(InterruptedException e1) {
//            e1.printStackTrace();
//        }
//        
//        TestCase.assertEquals("11", control1.getValue());
//        TestCase.assertEquals("22", control2.getValue());
//        TestCase.assertEquals("33", control3.getValue());
//        TestCase.assertEquals("4", control4.getValue());
//        
//
//        
//        
//    }
//    
//    
//
//        
//}
