package llc.ufwa.javacommon.test.collection;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import llc.ufwa.collections.BatchingIterator;

import org.junit.Test;

public class BatchingTest {

    @Test
    public void testBatcher() {
        
        final List<String> original = new ArrayList<String>();
        
        original.add("1");
        original.add("2");
        original.add("3");
        original.add("4");
        original.add("5");
        original.add("6");
        original.add("7");
        original.add("8");
        original.add("9");
        original.add("10");
        original.add("11");
        original.add("12");
        original.add("13");
        original.add("14");
        original.add("15");
        original.add("16");
        original.add("17");
        original.add("18");
        original.add("19");
        original.add("20");
        original.add("21");
        original.add("22");
        
        final BatchingIterator<String> batcher = new BatchingIterator<String>(original, 5);
        
        TestCase.assertTrue(batcher.hasNext());
        
        {
            
            final List<String> batch = batcher.next();
            
            TestCase.assertEquals(5, batch.size());
            TestCase.assertEquals("1", batch.get(0));
            TestCase.assertEquals("2", batch.get(1));
            TestCase.assertEquals("3", batch.get(2));
            TestCase.assertEquals("4", batch.get(3));
            TestCase.assertEquals("5", batch.get(4));
            
        }
        
        {
            
            TestCase.assertTrue(batcher.hasNext());
            
            final List<String> batch = batcher.next();
            
            TestCase.assertEquals(5, batch.size());
            TestCase.assertEquals("6", batch.get(0));
            TestCase.assertEquals("7", batch.get(1));
            TestCase.assertEquals("8", batch.get(2));
            TestCase.assertEquals("9", batch.get(3));
            TestCase.assertEquals("10", batch.get(4));
            
        }
        
        {
            
            TestCase.assertTrue(batcher.hasNext());
            
            final List<String> batch = batcher.next();
            
            TestCase.assertEquals(5, batch.size());
            TestCase.assertEquals("11", batch.get(0));
            TestCase.assertEquals("12", batch.get(1));
            TestCase.assertEquals("13", batch.get(2));
            TestCase.assertEquals("14", batch.get(3));
            TestCase.assertEquals("15", batch.get(4));
            
        }
        
        {
            TestCase.assertTrue(batcher.hasNext());
            
            final List<String> batch = batcher.next();
            
            TestCase.assertEquals(5, batch.size());
            TestCase.assertEquals("16", batch.get(0));
            TestCase.assertEquals("17", batch.get(1));
            TestCase.assertEquals("18", batch.get(2));
            TestCase.assertEquals("19", batch.get(3));
            TestCase.assertEquals("20", batch.get(4));
            
        }
        
        {
            TestCase.assertTrue(batcher.hasNext());
            
            final List<String> batch = batcher.next();
            
            TestCase.assertEquals(2, batch.size());
            TestCase.assertEquals("21", batch.get(0));
            TestCase.assertEquals("22", batch.get(1));
            
        }
        
        {
            
            TestCase.assertFalse(batcher.hasNext());
            
            final List<String> batch = batcher.next();
            
            TestCase.assertEquals(0, batch.size());
            
        }
        
    }
}
