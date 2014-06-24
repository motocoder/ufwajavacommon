package llc.ufwa.javacommon.test.concurrency;

import junit.framework.TestCase;
import llc.ufwa.concurrency.HitFlagger;

import org.junit.Test;

public class HitFlaggerTest {
    
    @Test
    public void testHitFlagger() {
        
        final HitFlagger flagger = new HitFlagger(500, 5);
        
        TestCase.assertFalse(flagger.hit());
        TestCase.assertFalse(flagger.hit());
        TestCase.assertFalse(flagger.hit());
        TestCase.assertFalse(flagger.hit());
        TestCase.assertTrue(flagger.hit());
        TestCase.assertTrue(flagger.hit());
        TestCase.assertTrue(flagger.hit());
        
        try {
            Thread.sleep(501);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        TestCase.assertFalse(flagger.hit());
        TestCase.assertFalse(flagger.hit());
        TestCase.assertFalse(flagger.hit());
        TestCase.assertFalse(flagger.hit());
        TestCase.assertTrue(flagger.hit());
        TestCase.assertTrue(flagger.hit());
        TestCase.assertTrue(flagger.hit());
        
        
    }

}
