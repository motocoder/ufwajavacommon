package llc.ufwa.javacommon.test.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;
import llc.ufwa.data.DefaultEntry;
import llc.ufwa.data.exception.HashBlobException;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.cache.FileHash;
import llc.ufwa.data.resource.cache.FileHashDataManager;
import llc.ufwa.data.resource.cache.HashDataManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.util.StreamUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHashTest {
    
    static {
        BasicConfigurator.configure();
    }
    
    private static final Logger logger = LoggerFactory.getLogger(FileHashTest.class);
    
    @Test
    public void testManager() {
        
        final File root = new File("./target/test-files/temp-hash/");
        final File tempFolder = new File("./target/test-files/temp-data");
        final File dataFolder = new File("./target/test-files/temp-data/data");
        
        deleteRoot(root);
        deleteRoot(tempFolder);
        deleteRoot(dataFolder);
        
        final FileHashDataManager<String> manager = new FileHashDataManager<String>(dataFolder, tempFolder, new SerializingConverter<String>());
        final Set<Entry<String, InputStream>> toSet = new HashSet<Entry<String, InputStream>>();
        
        toSet.add(new DefaultEntry<String, InputStream>("key", new ByteArrayInputStream("key".getBytes())));
        
        try {
            
            final int setAt = manager.setBlobs(-1, toSet);
            
            {
                
                TestCase.assertTrue(-1 != setAt);
                
                final Set<Entry<String, InputStream>> justSet = manager.getBlobsAt(setAt);
                
                TestCase.assertNotNull(justSet);
                TestCase.assertTrue(justSet.size() == 1);
                
                final Entry<String, InputStream> first = justSet.iterator().next();
                
                TestCase.assertEquals(first.getKey(), "key");
                
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                
                try {
                    StreamUtils.copy(first.getValue(), out);
                }
                catch (IOException e) {
                    TestCase.fail();
                }
                
                final String keyString = out.toString();
                
                TestCase.assertEquals("key", keyString);
            }
            
            logger.debug("step 2");
            
            {
                
                toSet.clear();
                
                toSet.add(new DefaultEntry<String, InputStream>("key", new ByteArrayInputStream("key".getBytes())));
                toSet.add(new DefaultEntry<String, InputStream>("key2", new ByteArrayInputStream("key2".getBytes())));
                
                final int setAt2 = manager.setBlobs(setAt, toSet);
                
                TestCase.assertTrue(setAt2 != setAt);
                
                final Set<Entry<String, InputStream>> justSet2 = manager.getBlobsAt(setAt2);
                final Set<Entry<String, InputStream>> justSet = manager.getBlobsAt(setAt); 
                
                TestCase.assertNull(justSet);
                TestCase.assertNotNull(justSet2);
                TestCase.assertTrue(justSet2.size() == 2);
                
                final Set<String> keys = new HashSet<String>();
                
                for(final Entry<String, InputStream> entry : justSet2) {
                    keys.add(entry.getKey());
                }

                TestCase.assertTrue(keys.contains("key"));
                TestCase.assertTrue(keys.contains("key2"));
                
                for(Entry<String, InputStream> entry : justSet2) {
                    
                    logger.debug("testing key " + entry.getKey());
                    
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    
                    try {
                        StreamUtils.copy(entry.getValue(), out);
                    }
                    catch (IOException e) {
                        TestCase.fail();
                    }
                    
                    final String keyString = out.toString();
                    
                    TestCase.assertEquals(entry.getKey(), keyString);
                    
                    logger.debug("key passed " + entry.getKey());
                    
                }
                
            }
            
            {
                
                toSet.clear();
                
                toSet.add(new DefaultEntry<String, InputStream>("key", new ByteArrayInputStream("key".getBytes())));
                
                final int setAt2 = manager.setBlobs(setAt, toSet);
                
                TestCase.assertTrue(setAt2 == setAt);
                
                final Set<Entry<String, InputStream>> justSet2 = manager.getBlobsAt(setAt2);
                
                TestCase.assertNotNull(justSet2);
                TestCase.assertTrue(justSet2.size() == 1);
                
                final Set<String> keys = new HashSet<String>();
                
                for(final Entry<String, InputStream> entry : justSet2) {
                    keys.add(entry.getKey());
                }

                TestCase.assertTrue(keys.contains("key"));
                
                for(Entry<String, InputStream> entry : justSet2) {
                    
                    logger.debug("testing key " + entry.getKey());
                    
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    
                    try {
                        StreamUtils.copy(entry.getValue(), out);
                    }
                    catch (IOException e) {
                        TestCase.fail();
                    }
                    
                    final String keyString = out.toString();
                    
                    TestCase.assertEquals(entry.getKey(), keyString);
                    
                    logger.debug("key passed " + entry.getKey());
                    
                }
                
            }
            
            logger.debug("step 3");
              
            {
                
                toSet.clear();
                
                toSet.add(new DefaultEntry<String, InputStream>("key", new ByteArrayInputStream("key".getBytes())));
                toSet.add(new DefaultEntry<String, InputStream>("key2", new ByteArrayInputStream("key2".getBytes())));
                
                final int setAt2 = manager.setBlobs(-1, toSet);
                
                TestCase.assertTrue(setAt2 != -1);
                
                final Set<Entry<String, InputStream>> justSet2 = manager.getBlobsAt(setAt2);
               
                TestCase.assertNotNull(justSet2);
                TestCase.assertTrue(justSet2.size() == 2);
                
                final Set<String> keys = new HashSet<String>();
                
                for(final Entry<String, InputStream> entry : justSet2) {
                    keys.add(entry.getKey());
                }

                TestCase.assertTrue(keys.contains("key"));
                TestCase.assertTrue(keys.contains("key2"));
                
                for(Entry<String, InputStream> entry : justSet2) {
                    
                    logger.debug("testing key " + entry.getKey());
                    
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    
                    try {
                        StreamUtils.copy(entry.getValue(), out);
                    }
                    catch (IOException e) {
                        TestCase.fail();
                    }
                    
                    final String keyString = out.toString();
                    
                    TestCase.assertEquals(entry.getKey(), keyString);
                    
                    logger.debug("key passed " + entry.getKey());
                    
                }
                
            }
            
            logger.debug("step 5");
            
            final Map<Integer, Integer> indexes = new HashMap<Integer, Integer>();
            
            final int TEST_SIZE = 100;
            
            for(int i = 0; i < TEST_SIZE; i++) {
                
                final Set<Entry<String, InputStream>> value = new HashSet<Entry<String, InputStream>>();
                
                value.add(new DefaultEntry<String, InputStream>(String.valueOf(i), new ByteArrayInputStream(String.valueOf(i).getBytes())));
                
                int blobIndex = manager.setBlobs(-1, value);
                
                logger.debug("blobIndex for key " + i);
                
                indexes.put(i, blobIndex);
                
            }
            
            for(int i = 0; i < TEST_SIZE; i++) {
                
                logger.debug("testing key " + String.valueOf(i));
                
                final int blobIndex = indexes.get(i);
                
                final Set<Entry<String, InputStream>> value = manager.getBlobsAt(blobIndex);
                
                TestCase.assertTrue(value.iterator().hasNext());
                
                final Entry<String, InputStream> entry = value.iterator().next();
                
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                
                try {
                    StreamUtils.copy(entry.getValue(), out);
                }
                catch (IOException e) {
                    TestCase.fail();
                }
                
                final String keyString = out.toString();
                
                TestCase.assertEquals(entry.getKey(), keyString);
                
                logger.debug("key passed " + entry.getKey());
                
            }
                
            
        } 
        catch (HashBlobException e) {
            
            logger.error("failed", e);
            TestCase.fail();
        }
        
    }
    
    @Test
    public void testHashing() {
        
        final File root = new File("./target/test-files/temp-hash/");
        deleteRoot(root);
        
        final FileHash<String, String> hash = new FileHash<String, String>(root, new FakeHashManagerImpl<String, String>(), 1000);
        
        checkFileEmpty(root);
        
        final int TEST_COUNT = 10;
        
        for(int i = 0; i < TEST_COUNT; i++) {
        
            hash.put(String.valueOf(i), String.valueOf(i));
            
            final String seg = hash.get(String.valueOf(i));
            
            TestCase.assertNotNull("i not null " + i, seg);
            
            TestCase.assertEquals(String.valueOf(i), seg);
            
        }
        
        TestCase.assertNull(hash.get(String.valueOf(TEST_COUNT + 1)));
        
        for(int i = 0; i < TEST_COUNT; i++) {
            hash.remove(String.valueOf(i));            
        }
        
        for(int i = 0; i < TEST_COUNT; i++) {
            
            final String val = hash.get(String.valueOf(i));
            
            TestCase.assertNull(val);
            
        }
        
        checkFileEmpty(root);
        
        deleteRoot(root);
        
    }
    
    @Test
    public void testRealHashing() {
        
        final File root = new File("./target/test-files/temp-hash/");
        final File tempFolder = new File("./target/test-files/temp-data");
        final File dataFolder = new File("./target/test-files/temp-data/data");
        
        deleteRoot(root);
        deleteRoot(tempFolder);
        deleteRoot(dataFolder);
        
        final HashDataManager<String, InputStream> manager = new LoggingManager(new FileHashDataManager<String>(dataFolder, tempFolder, new SerializingConverter<String>()));
        
        final FileHash<String, InputStream> hash = new FileHash<String, InputStream>(root, manager, 1000);
        
        checkFileEmpty(root);
        
        final int TEST_COUNT = 100;
        
        for(int i = 0; i < TEST_COUNT; i++) {
        
            final ByteArrayInputStream input = new ByteArrayInputStream(String.valueOf(i).getBytes());
            
            hash.put(String.valueOf(i), input);
            
            final InputStream segInput = hash.get(String.valueOf(i));
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            try {
                StreamUtils.copy(segInput, out);
            }
            catch(IOException e) {
                TestCase.fail();
            }
            
            final String seg = new String(out.toByteArray());
            
            TestCase.assertNotNull("i not null " + i, seg);
            
            TestCase.assertEquals(String.valueOf(i), seg);
            
        }
        
        TestCase.assertNull(hash.get(String.valueOf(TEST_COUNT + 1)));
        
        for(int i = 0; i < TEST_COUNT; i++) {
            hash.remove(String.valueOf(i));            
        }
        
        for(int i = 0; i < TEST_COUNT; i++) {
            
            final InputStream segInput = hash.get(String.valueOf(i));
            
            TestCase.assertNull(segInput);
            
        }
        
        checkFileEmpty(root);
        
        deleteRoot(root);
        deleteRoot(tempFolder);
        deleteRoot(dataFolder);
        
    }
    
    private static class LoggingManager implements HashDataManager<String, InputStream> {
        
        private final HashDataManager<String, InputStream> internal;

        public LoggingManager(HashDataManager<String, InputStream> internal) {
            this.internal = internal;
        }

        @Override
        public Set<Entry<String, InputStream>> getBlobsAt(int blobIndex) throws HashBlobException {
            return internal.getBlobsAt(blobIndex);
        }

        @Override
        public int setBlobs(int blobIndex, Set<Entry<String, InputStream>> blobs) throws HashBlobException {
            
            logger.debug("setting blobs " + blobIndex);
            
            final int returnVal = internal.setBlobs(blobIndex, blobs);
            
            logger.debug("set blobs at index " + blobIndex + " new Index " + returnVal);
            
            return returnVal;
            
        }

        @Override
        public void eraseBlobs(int blobIndex) throws HashBlobException {
            
            internal.eraseBlobs(blobIndex);
            
        }
    }
    
    private void checkFileEmpty(final File root) {
        
        try {
            
            final InputStream in = new FileInputStream(root);
            
            try {
                
                int totalRead = 0;
                
                final byte [] buffer = new byte[1000];
                
                while(true) {
                    
                    final int read = in.read(buffer);
                    
                    if(read > 0) {
                        
                        for(int i = 0; i < read; i++) {
                            
                            if(buffer[i] != -1) {
                                
                                logger.debug("hash not empty " + totalRead + " " + buffer[i]);
                                TestCase.fail();
                                
                            }
                            
                            totalRead++;
                            
                        }
                        
                    }
                    else {
                        break;
                    }
                    
                }
                
            }
            finally {
                in.close();
            }
        } 
        catch (FileNotFoundException e) {
            TestCase.fail();
        }
        catch (IOException e) {
            TestCase.fail();
        }
        
    }

    void deleteRoot (File root) {
        if (root.exists()) {
            
            if(root.listFiles() != null) {
                for (File cacheFile: root.listFiles()){
                    cacheFile.delete();
                }
            }
            root.delete();
        }
    }

}
