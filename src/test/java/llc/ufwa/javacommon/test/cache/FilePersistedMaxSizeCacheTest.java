package llc.ufwa.javacommon.test.cache;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.StringSizeConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.CacheFactory;
import llc.ufwa.data.resource.cache.FileHashCache;
import llc.ufwa.data.resource.cache.FilePersistedMaxSizeCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;
import llc.ufwa.util.StringUtilities;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePersistedMaxSizeCacheTest {

	private static final Logger logger = LoggerFactory.getLogger(FilePersistedMaxSizeCacheTest.class);

	private static final byte[] TEN_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

	static {
		BasicConfigurator.configure();
	}

	@Test
	public void testPersistedPart() {

		Random random = new Random();
		String appendix = String.valueOf(Math.abs(random.nextInt()));

		File root2 = new File("./target/test-files/temp" + appendix + "/");

		deleteRoot(root2);

		final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(
				new StringSizeConverter());

		final File dataFolder = new File(root2, "data");
		final File tempFolder = new File(root2, "temp");
		final File persistingFolder = new File(root2, "persisting");

		FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);

		Cache<String, String> fileCache = new ValueConvertingCache<String, String, byte[]>(
				new ValueConvertingCache<String, byte[], InputStream>(diskCache,
						new ReverseConverter<byte[], InputStream>(new InputStreamConverter())),
				new SerializingConverter<String>());

		Cache<String, String> cache = new FilePersistedMaxSizeCache<String>(persistingFolder,
				fileCache, converter, 150);

		try {

			cache.clear();

			final String TEN_BYTES_STRING = new String(TEN_BYTES);

			cache.put("1", TEN_BYTES_STRING);

			Thread.sleep(100);

			cache.put("2", TEN_BYTES_STRING);

			TestCase.assertNotNull(cache.get("1"));
			TestCase.assertNotNull(cache.get("2"));

			cache.put("3", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("4", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("5", TEN_BYTES_STRING);

			TestCase.assertNull(cache.get("1"));
			TestCase.assertNull(cache.get("2"));
			TestCase.assertNull(cache.get("3"));
			TestCase.assertNotNull(cache.get("4"));
			TestCase.assertNotNull(cache.get("5"));

			cache = null;
			fileCache = null;
			diskCache = null;

			Thread.sleep(500);

			TestCase.assertNull(cache);

			diskCache = new FileHashCache(dataFolder, tempFolder);

			fileCache = new ValueConvertingCache<String, String, byte[]>(
					new ValueConvertingCache<String, byte[], InputStream>(diskCache,
							new ReverseConverter<byte[], InputStream>(new InputStreamConverter())),
					new SerializingConverter<String>());

			cache = new FilePersistedMaxSizeCache<String>(persistingFolder, fileCache, converter,
					150);

			TestCase.assertTrue(cache.exists("4"));
			TestCase.assertNotNull(cache.get("5"));

			cache.put("6", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("7", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("8", TEN_BYTES_STRING);

		}
		catch (InterruptedException e1) {
			TestCase.fail();
		}
		catch (ResourceException e) {
			TestCase.fail();
		}

		try {

			TestCase.assertFalse(cache.exists("4"));
			TestCase.assertNull(cache.get("5"));
			TestCase.assertNull(cache.get("6"));
			TestCase.assertNotNull(cache.get("7"));
			TestCase.assertNotNull(cache.get("8"));

		}
		catch (ResourceException e) {

			TestCase.fail("cant get here");
			e.printStackTrace();

		}

		deleteRoot(root2);

	}

	@Test
	public void testMaxSizePart() {

		Random random = new Random();
		String appendix = String.valueOf(Math.abs(random.nextInt()));

		File root2 = new File("./target/test-files/temp" + appendix + "/");

		deleteRoot(root2);

		final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(
				new StringSizeConverter());

		final File dataFolder = new File(root2, "data");
		final File tempFolder = new File(root2, "temp");
		final File persistingFolder = new File(root2, "persisting");

		final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);

		final Cache<String, String> fileCache = new ValueConvertingCache<String, String, byte[]>(
				new ValueConvertingCache<String, byte[], InputStream>(diskCache,
						new ReverseConverter<byte[], InputStream>(new InputStreamConverter())),
				new SerializingConverter<String>());

		final Cache<String, String> cache = new FilePersistedMaxSizeCache<String>(persistingFolder,
				fileCache, converter, 150);

		try {

			cache.clear();

			final String TEN_BYTES_STRING = new String(TEN_BYTES);

			cache.put("1", TEN_BYTES_STRING);

			Thread.sleep(100);

			cache.put("2", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("3", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("4", TEN_BYTES_STRING);

			Thread.sleep(50);

			cache.put("5", TEN_BYTES_STRING);

		}
		catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		catch (ResourceException e) {
			e.printStackTrace();
		}

		try {

			TestCase.assertFalse(cache.exists("1"));
			TestCase.assertNull(cache.get("2"));
			TestCase.assertNull(cache.get("3"));
			TestCase.assertNotNull(cache.get("4"));
			TestCase.assertNotNull(cache.get("5"));

		}
		catch (ResourceException e) {

			TestCase.fail("cant get here");
			e.printStackTrace();

		}

		deleteRoot(root2);

	}

	@Test
	public void testFilePersistedExpiringCacheTest() {

		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));

			File root = new File("./target/test-files/temp" + appendix + "/");

			deleteRoot(root);

			final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(
					new StringSizeConverter());

			final File dataFolder = new File(root, "data");
			final File tempFolder = new File(root, "temp");
			final File persistingFolder = new File(root, "persisting");

			final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);

			final Cache<String, String> fileCache = new ValueConvertingCache<String, String, byte[]>(
					new ValueConvertingCache<String, byte[], InputStream>(diskCache,
							new ReverseConverter<byte[], InputStream>(new InputStreamConverter())),
					new SerializingConverter<String>());

			final Cache<String, String> cache = new FilePersistedMaxSizeCache<String>(
					persistingFolder, fileCache, converter, 1000);

			final String key = "dfsa";
			final String value = "dfsadsf";
			final String key2 = "fgdd";
			final String value2 = "dfgsds";
			String returnValue;

			// TEST PUT, GET, REMOVE, and EXISTS

			cache.put(key, value);

			returnValue = cache.get(key);

			TestCase.assertEquals(value, returnValue);
			TestCase.assertEquals(cache.exists(key), true);

			cache.remove(key);

			TestCase.assertEquals(cache.exists(key), false);

			// TEST CLEAR, GETALL, and RETEST EXISTS AND GET

			List<String> keyList = new ArrayList<String>();

			keyList.add(key);
			keyList.add(key2);

			cache.put(key, value);
			cache.put(key2, value2);
			cache.put(key2, value2); //repeat to test the automatic remove() when duplicate

			TestCase.assertEquals(cache.exists(key), true);
			TestCase.assertEquals(cache.exists(key2), true);

			List<String> stringList = cache.getAll(keyList);

			for (int y = 0; y < 1; y++) {

				returnValue = stringList.get(y);

				TestCase.assertEquals(value, returnValue);
				TestCase.assertEquals(cache.exists(key), true);

			}

			cache.clear();

			TestCase.assertEquals(cache.exists(key), false);
			TestCase.assertEquals(cache.get(key), null);

			deleteRoot(root);

		}
		catch (ResourceException e) {
			e.printStackTrace();
			TestCase.fail();
		}

	}

	@Test
	public void testFilePersistingSizeCacheTest() {

		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));

			File root = new File("./target/test-files/temp" + appendix + "/");

			deleteRoot(root);

			final File dataFolder = new File(root, "data");

			final int maxSize = 5000;
			final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(
					new StringSizeConverter());

			final Cache<String, String> cache = CacheFactory.getMaxSizeFileCache(maxSize,
					dataFolder, converter);

			final String key = "dfslkjasdfkljsadfa";
			final String value = "dfsaoiuwekljfsdfsadlkaioklalkdsf";

			for (int x = 0; x < 10; x++) {

				final String keyRepeated = StringUtilities.repeat(key, x);
				final String valueRepeated = StringUtilities.repeat(value, x);

				cache.put(keyRepeated, valueRepeated);

				Thread.sleep(5);

			}

			// test exists
			for (int x = 0; x < 10; x++) {

				final String keyRepeated = StringUtilities.repeat(key, x);
				final String valueRepeated = StringUtilities.repeat(value, x);

				TestCase.assertEquals(true, cache.exists(keyRepeated));
				TestCase.assertEquals(valueRepeated, cache.get(keyRepeated));

			}

			for (int x = 0; x < 100; x++) {

				final String keyRepeated = StringUtilities.repeat(key, x);

				cache.put(keyRepeated, value);

				logger.debug("Size of data " + folderSize(dataFolder));

			}

			// test exists
			for (int x = 0; x < 10; x++) {

				final String keyRepeated = StringUtilities.repeat(key, x);
				TestCase.assertEquals(false, cache.exists(keyRepeated));

			}

			final String key2 = StringUtilities.repeat(key, 1);

			TestCase.assertEquals(false, cache.exists(key2));

			deleteRoot(root);

		}
		catch (ResourceException e) {
			e.printStackTrace();
			TestCase.fail();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFilePersistingSizeCacheMultithreadedTest() {

		final Executor exec = Executors.newFixedThreadPool(10);

		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));

			File root = new File("./target/test-files/temp" + appendix + "/");

			deleteRoot(root);

			final File dataFolder = new File(root, "data");

			final int maxSize = 5000;
			final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(
					new StringSizeConverter());

			final Cache<String, String> cache = CacheFactory.getMaxSizeFileCache(maxSize,
					dataFolder, converter);

			final String key = "dfslkjasdfkljsadfa";
			final String value = "dfsaoiuwekljfsdfsadlkaioklalkdsf";

			for (int x = 0; x < 10; x++) {

				final int xfinal = x;

				exec.execute(new Runnable() {

					@Override
					public void run() {

						final String keyRepeated = StringUtilities.repeat(key, xfinal);
						final String valueRepeated = StringUtilities.repeat(value, xfinal);

						try {
							cache.put(keyRepeated, valueRepeated);
						}
						catch (ResourceException e) {
							e.printStackTrace();
						}

					}

				});

			}

			Thread.sleep(3000);

			for (int x = 0; x < 10; x++) {

				final int xfinal = x;

				exec.execute(new Runnable() {

					@Override
					public void run() {

						final String keyRepeated = StringUtilities.repeat(key, xfinal);
						final String valueRepeated = StringUtilities.repeat(value, xfinal);

						try {
							TestCase.assertEquals(true, cache.exists(keyRepeated));
							TestCase.assertEquals(valueRepeated, cache.get(keyRepeated));
						}
						catch (ResourceException e) {
							e.printStackTrace();
						}

					}

				});

			}

			for (int x = 500; x < 560; x++) {

				final int xfinal = x;

				exec.execute(new Runnable() {

					@Override
					public void run() {

						final String keyRepeated = StringUtilities.repeat(key, xfinal);

						try {
							cache.put(keyRepeated, value);
						}
						catch (ResourceException e) {
							e.printStackTrace();
						}

						logger.debug("Size of data " + folderSize(dataFolder));

					}

				});

			}

			Thread.sleep(10000);

			// test exists
			for (int x = 0; x < 10; x++) {

				final String keyRepeated = StringUtilities.repeat(key, x);
				TestCase.assertEquals(false, cache.exists(keyRepeated));

			}

			final String key2 = StringUtilities.repeat(key, 1);

			TestCase.assertEquals(false, cache.exists(key2));

			deleteRoot(root);

		}
		catch (ResourceException e) {
			e.printStackTrace();
			TestCase.fail();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void universalTest() {

		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));

			File root = new File("./target/test-files/temp" + appendix + "/");

			deleteRoot(root);

			final File dataFolder = new File(root, "data");

			final int maxSize = 5000;
			final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(
					new StringSizeConverter());

			final Cache<String, String> cache = CacheFactory.getMaxSizeFileCache(maxSize,
					dataFolder, converter);

			final String key = "dfsa";
			final String value = "dfsadsf";
			final String key2 = "fgdd";
			final String value2 = "dfgsds";

			// TEST PUT, GET, REMOVE, and EXISTS

			cache.put(key, value);

			String retVal = cache.get(key);

			TestCase.assertEquals(value, retVal);
			TestCase.assertEquals(cache.exists(key), true);

			cache.remove(key);

			TestCase.assertEquals(cache.exists(key), false);

			// TEST CLEAR, GETALL, and RETEST EXISTS

			List<String> keyList = new ArrayList<String>();

			keyList.add(key);
			keyList.add(key2);

			cache.put(key, value);
			cache.put(key2, value2);

			TestCase.assertEquals(cache.exists(key), true);
			TestCase.assertEquals(cache.exists(key2), true);

			List<String> streamList = cache.getAll(keyList);

			for (int y = 0; y < 1; y++) {

				String ret = streamList.get(y);

				TestCase.assertEquals(value, ret);
				TestCase.assertEquals(cache.exists(key), true);

			}

			cache.clear();

			TestCase.assertEquals(cache.exists(key), false);
			TestCase.assertEquals(cache.exists(key2), false);

		}
		catch (ResourceException e) {
			e.printStackTrace();
		}

	}

	@Test 
    public void testFileMaxSizeCacheTest() {
        
        try {

            Random random = new Random();
            String appendix = String.valueOf(Math.abs(random.nextInt()));
            
            File root = new File("./target/test-files/temp" + appendix + "/");
            
            deleteRoot(root);
            
            final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(new StringSizeConverter());
            
            final File dataFolder = new File(root, "data");
            final File tempFolder = new File(root, "temp");
            final File persistingFolder = new File(root, "persisting");
                        
            final File persistingFolderData = new File(persistingFolder, "sizePersisted/data/data");
            final File dataFolderData = new File(root, "data/data");
            final File tempFolderData = new File(root, "temp/data");
            
            final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);
            
            final Cache<String, String> fileCache = 
                new ValueConvertingCache<String, String, byte []>(
                    new ValueConvertingCache<String, byte [], InputStream>(
                            diskCache,
                            new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
                        ),
                        new SerializingConverter<String>()
                    );
                
            final Cache<String, String> cache =
                new FilePersistedMaxSizeCache<String>(
                    persistingFolder,
                    fileCache,
                    converter,
                    100000
                );
            
            
            final String value = StringUtilities.repeat("dfsaoiuwekljfsdfsadlkaioklalkdsf", 1000);
            
            
            for (int x = 0; x < 10; x++) {
                
                final String keyRepeated = String.valueOf("x");
                
                cache.put(keyRepeated, value);
                
                Thread.sleep(50);
                
            }
            
            for (int x = 0; x < 10000; x++) {
                
                final String keyRepeated = String.valueOf(x);
                
                cache.put(keyRepeated, value);
                
                if(x % 100 == 0) {
                    
                    logger.debug(x + " Size of persisting data " + persistingFolderData.length());
                    logger.debug(x + " Size of data " + dataFolderData.length());
                    logger.debug(x + " Size of temp " + tempFolderData.length());
                    
                }
                
            }
            
            deleteRoot(root);
        
        }
        catch (ResourceException e) {
            
            e.printStackTrace();
            TestCase.fail();
            
        }
        catch (InterruptedException e) {
            
            e.printStackTrace();
            TestCase.fail();
            
        }
        
    }

	@Test
	public void negativeSizeTest() {

		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));

			File root = new File("./target/test-files/temp" + appendix + "/");

			deleteRoot(root);

			final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(
					new StringSizeConverter());

			final File dataFolder = new File(root, "data");
			final File tempFolder = new File(root, "temp");
			final File persistingFolder = new File(root, "persisting");

			final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);

			final Cache<String, String> fileCache = new ValueConvertingCache<String, String, byte[]>(
					new ValueConvertingCache<String, byte[], InputStream>(diskCache,
							new ReverseConverter<byte[], InputStream>(new InputStreamConverter())),
					new SerializingConverter<String>());

			final Cache<String, String> cache = new FilePersistedMaxSizeCache<String>(
					persistingFolder, fileCache, converter, 200);

			final String key = "dfsa";
			final String key1 = "dfewfawfsdsfsdfadsadsfvsa";
			final String key2 = "dfsewr34fara";
			final String value2 = "dsfaskdfaskfjhasjkdfhaskfjldhaskfjhaskjfhkashdfkasjhdfkahsdfkljhs";
			final String key4 = "9un98q5n3miodfsa";
			final String value = "qv54v3ckljhdsfoyh43ods";
			String returnValue;

			for (int i = 0; i < 20; i++) {

				cache.put(key, value);
				cache.put(key1, value2);
				cache.put(key2, value2);
				cache.put(key4, value);

				returnValue = cache.get(key4);

				TestCase.assertEquals(value, returnValue);
				TestCase.assertEquals(cache.exists(key4), true);

				cache.remove(key4);

				TestCase.assertEquals(cache.exists(key4), false);

				logger.debug("Size data " + new File(dataFolder, "data").length());
				logger.debug("Size persistingFolder " + new File(root, "persisting").length());
				logger.debug("Size tempFolder " + new File(root, "temp").length());

			}

			deleteRoot(root);

		}
		catch (ResourceException e) {

			e.printStackTrace();
			TestCase.fail();
		}

	}

	public static long folderSize(File directory) {
		long length = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				logger.debug("size of individual file " + file.getAbsolutePath() + " is " + file.length());
				length += file.length();
			}
			else {
				length += folderSize(file);
			}
		}
		return length;
	}

	void deleteRoot(File root) {

		if (root.exists()) {

			final File[] fileList = root.listFiles();

			if (fileList != null) {

				for (File cacheFile : fileList) {
					cacheFile.delete();
				}

			}

			root.delete();

		}
	}

}
