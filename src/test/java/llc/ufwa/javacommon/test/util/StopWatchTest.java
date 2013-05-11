package llc.ufwa.javacommon.test.util;

import junit.framework.TestCase;
import llc.ufwa.util.StopWatch;

import org.junit.Test;

public class StopWatchTest
{
    
    @Test
    public void testStopWatch() {
        
        final StopWatch stopWatch = new StopWatch();
        
        try {
            
            stopWatch.getTime();
            TestCase.fail("Get time should have thrown exception if StopWatch wasn't started yet");
            
        }
        catch(IllegalStateException t) {
            //CorrectBehavior
        }
        
        
        stopWatch.start();
        
        try
        {
            Thread.sleep(50);
        }
        catch (InterruptedException e)
        {
            TestCase.fail("This should never happen");
        }
        
        final long time1 = stopWatch.getTime();
        
        try
        {
            Thread.sleep(50);
        }
        catch (InterruptedException e)
        {
            TestCase.fail("This should never happen");
        }
        
        final long time2 = stopWatch.getTime();
        
        TestCase.assertTrue(time1 > 0 && time2 > 0);
        TestCase.assertTrue(time1 < time2);
        
        stopWatch.stop();
        
        final long time3 = stopWatch.getTime();
        final long time4 = stopWatch.getTime();
        
        TestCase.assertTrue(time3 == time4);
        
        try {
            
            stopWatch.stop();
            TestCase.fail("You cannot call stop twice!");
            
        }
        catch(IllegalStateException t) {
            //CorrectBehavior
        }
    }

}
