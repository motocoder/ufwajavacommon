package llc.ufwa.javacommon.test.concurrency;

import junit.framework.TestCase;
import llc.ufwa.concurrency.Locks;
import llc.ufwa.concurrency.ParallelControl;

import org.junit.Test;

public class LocksTest {
    
    @Test
    public void testLocks() {
        
        for(int i = 0; i < 20; i++) {
            
            try {
                
                final Locks locks = new Locks();
                
                locks.getLock("lock");
                
                final ParallelControl<String> control = new ParallelControl<String>();
                
                new Thread() {
                    
                    @Override
                    public void run() {
                        
                        try {
                            
                            control.unBlockOnce();
                            
                            locks.getLock("lock");
                            
                            control.setValue("notBlocked");
                            
                            control.blockOnce();
                            locks.releaseLock("lock");
                            
                        } 
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        
                    }
                    
                }.start();
                
                final ParallelControl<String> control2 = new ParallelControl<String>();
                
                new Thread() {
                    
                    @Override
                    public void run() {
                        
                        try {
                            
                            control2.blockOnce();
                            
                            locks.getLock("lock");
                            
                            control2.setValue("notBlocked");
                            control2.blockOnce();
                            
                            locks.releaseLock("lock");
                            
                        } 
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        
                    }
                    
                }.start();
            
                control.blockOnce();
                
                Thread.sleep(10);
                
                control2.unBlockOnce();
                
                Thread.sleep(10);
                
                TestCase.assertNull(control.getValue());
                TestCase.assertNull(control2.getValue());
                
                locks.releaseLock("lock");
                
                Thread.sleep(10);
                
                TestCase.assertNotNull(control.getValue());
                TestCase.assertNull(control2.getValue());
                
                control.unBlockOnce();
                
                Thread.sleep(10);
                
                TestCase.assertNotNull(control2.getValue());
                
                control2.unBlockOnce();
                
                locks.getLock("lock");
            
                
            }
            catch(Exception e) {
                e.printStackTrace();
                TestCase.fail("Should not have gotten here");
            }
            
        }        
        
    }

}
