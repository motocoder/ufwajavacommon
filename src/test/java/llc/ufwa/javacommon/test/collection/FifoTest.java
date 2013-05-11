package llc.ufwa.javacommon.test.collection;

import java.util.LinkedList;

import junit.framework.TestCase;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.FifoCache;
import llc.ufwa.data.resource.cache.MemoryCache;
import llc.ufwa.data.resource.provider.PushProvider;
import llc.ufwa.data.resource.provider.SettableResourceProvider;

import org.junit.Test;

public class FifoTest {
    
    @Test
    public void testFifo() {
        
        final Cache<Long, String> cache = new MemoryCache<Long, String>();
        final PushProvider<LinkedList<Long>> provider = new SettableResourceProvider<LinkedList<Long>>();
        
        try {
            provider.push(new LinkedList<Long>());
        } catch (ResourceException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        final FifoCache<String> fifo = new FifoCache<String>(provider, cache);
        
        fifo.offer("1");
        
        try {
            
            TestCase.assertEquals(1, provider.provide().size());
            TestCase.assertEquals(fifo.size(), provider.provide().size());
            TestCase.assertEquals("1", cache.get(provider.provide().get(0)));
            
            fifo.offer("2");
            
            TestCase.assertEquals(2, provider.provide().size());
            TestCase.assertEquals("1", cache.get(provider.provide().get(0)));
            TestCase.assertEquals("2", cache.get(provider.provide().get(1)));
            
            fifo.offer("3");
            
            TestCase.assertEquals(3, provider.provide().size());
            TestCase.assertEquals("1", cache.get(provider.provide().get(0)));
            TestCase.assertEquals("2", cache.get(provider.provide().get(1)));
            TestCase.assertEquals("3", cache.get(provider.provide().get(2)));
            
            fifo.poll();
            
            TestCase.assertEquals(2, provider.provide().size());
            TestCase.assertEquals("2", cache.get(provider.provide().get(0)));
            TestCase.assertEquals("3", cache.get(provider.provide().get(1)));
            
            fifo.offer("4");
            
            TestCase.assertEquals(3, provider.provide().size());
            TestCase.assertEquals("2", cache.get(provider.provide().get(0)));
            TestCase.assertEquals("3", cache.get(provider.provide().get(1)));
            TestCase.assertEquals("4", cache.get(provider.provide().get(2)));
            
            fifo.poll();
            
            TestCase.assertEquals(2, provider.provide().size());
            TestCase.assertEquals("3", cache.get(provider.provide().get(0)));
            TestCase.assertEquals("4", cache.get(provider.provide().get(1)));
            
            fifo.poll();
            
            TestCase.assertEquals(1, provider.provide().size());
            TestCase.assertEquals("4", cache.get(provider.provide().get(0)));
            
            fifo.poll();
            
            TestCase.assertEquals(0, provider.provide().size());            
            TestCase.assertNull(fifo.poll());            
            TestCase.assertEquals(0, provider.provide().size());
            
            TestCase.assertTrue(provider.provide().isEmpty());
            TestCase.assertTrue(fifo.isEmpty());
            
        } 
        catch (ResourceException e) {
            TestCase.fail();
        }
           
    }
    
}
