package llc.ufwa.javacommon.test.resource;

import java.io.File;
import java.util.LinkedList;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.DiskCache;
import llc.ufwa.data.resource.cache.KeyEncodingCache;
import llc.ufwa.data.resource.cache.MemoryCache;
import llc.ufwa.data.resource.cache.SynchronizedCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;
import llc.ufwa.data.resource.provider.CachedPushProvider;
import llc.ufwa.data.resource.provider.PushProvider;
import llc.ufwa.data.resource.provider.ResourceProvider;
import llc.ufwa.data.resource.provider.SynchronizedPushProvider;

import org.junit.Test;

public class CachedPushProviderTest {

    @Test
    public void testCachedProvider() {
        
        final ResourceProvider<String> root = 
            new ResourceProvider<String>() {

                @Override
                public boolean exists() throws ResourceException {
                    return true;
                }
    
                @Override
                public String provide() throws ResourceException {
                    return "Hi";
                }
                
            };
        
        final PushProvider<String> provider =
            new CachedPushProvider<String>(new MemoryCache<String, String>(), root);
        
        try {
            
            TestCase.assertEquals(provider.provide(), "Hi");
            
            provider.push("Hi2");
            
            TestCase.assertEquals(provider.provide(), "Hi2");
            
        } 
        catch (ResourceException e) {
            
            e.printStackTrace();
            TestCase.fail();
            
        }
        
    }
    
    @Test
    public void test2() {
        
        final Cache<String, LinkedList<Long>> listCache;
        final PushProvider<LinkedList<Long>> provider;
        
        {
            
            final File listCacheDir = new File("./target/test-files/temp");
            final Cache<String, byte []> listDiskCache = new DiskCache<String>(listCacheDir, -1L, -1L);
            
            listCache = 
                new SynchronizedCache<String, LinkedList<Long>>(
                    new ValueConvertingCache<String, LinkedList<Long>, byte []>(
                            new KeyEncodingCache<byte []>(
                                listDiskCache
                            ),
                        new SerializingConverter<LinkedList<Long>>()
                    )
                );
         
        }
        
        {
            
            final ResourceProvider<LinkedList<Long>> rootProvider =
                new ResourceProvider<LinkedList<Long>>() {

                    @Override
                    public LinkedList<Long> provide() throws ResourceException {
                        return new LinkedList<Long>();
                    }

                    @Override
                    public boolean exists() throws ResourceException {
                        return true;
                    }
                    
                };
                
            provider = new SynchronizedPushProvider<LinkedList<Long>>(
                new CachedPushProvider<LinkedList<Long>>(listCache, rootProvider)
            );
            
        }
        
        try {
            
            TestCase.assertEquals(0, provider.provide().size());
            
            final LinkedList<Long> list = new LinkedList<Long>();
            
            list.add(0L);
            
            provider.push(list);
            
            TestCase.assertEquals(1, provider.provide().size());
            
        } 
        catch (ResourceException e) {
            TestCase.fail();
        }
        finally {
            
        }
        
        
        
        
    }
}
