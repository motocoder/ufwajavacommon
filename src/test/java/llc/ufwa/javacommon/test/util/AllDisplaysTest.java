package llc.ufwa.javacommon.test.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.activities.injecting.AllDisplays;
import llc.ufwa.activities.injecting.InjectingDisplay;
import llc.ufwa.concurrency.ParallelControl;

import org.junit.Test;

public class AllDisplaysTest {
    
    @Test
    public void testAllDisplays() {
        
        final List<ParallelControl<String>> controllers = new ArrayList<ParallelControl<String>>();
        
        final AllDisplays<MyDisplay> allDisplays = new AllDisplays<MyDisplay>(MyDisplay.class);
        
        for(int i = 0; i < controllers.size(); i++) {
            
            final ParallelControl<String> controller = controllers.get(i);
            
            allDisplays.addDisplay(
                new MyDisplay() {

                    @Override
                    public void myMethod(String value) {
                        
                        controller.setValue(value);
                        controller.unBlockOnce();
                        
                    }
                    
                }
                
            );
            
        }
        
        final MyDisplay display = allDisplays.getAllDisplays();
        
        display.myMethod("myValue");
        
        for(final ParallelControl<String> controller : controllers) {
            
            try {
                controller.blockOnce();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            TestCase.assertEquals("myValue", controller.getValue());
            
        }
        
    }
    
    private interface MyDisplay extends InjectingDisplay {
        
        void myMethod(String value);
        
    }

}
