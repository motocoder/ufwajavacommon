package llc.ufwa.javacommon.test.concurrency;

import java.util.concurrent.Executors;

import junit.framework.TestCase;

import llc.ufwa.concurrency.Callback;
import llc.ufwa.concurrency.Debouncer;
import llc.ufwa.concurrency.ParallelControl;

import org.junit.Test;

public class DebouncerTest {

    @Test 
    public void testDebouncer() {
        
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
                
                return null;
                
            }}, Executors.newFixedThreadPool(10), 1000);
        
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
        
        debouncer.signal();
        
        try {
            Thread.sleep(1100);
        } 
        catch (InterruptedException e) {
            TestCase.fail();
        }
        
        TestCase.assertEquals((int)control.getValue(), 3);
        
    }
}
