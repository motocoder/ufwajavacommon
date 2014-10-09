package llc.ufwa.javacommon.test.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import llc.ufwa.concurrency.Callback;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.StringSizeConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.CacheFactory;
import llc.ufwa.util.StringUtilities;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePersistedExpiringCacheTest {

	private static final Logger logger = LoggerFactory.getLogger(FilePersistedExpiringCacheTest.class);

	@Test
	public void FilePersistedExpiringCacheMultithreadedTest() {

		final Executor exec = Executors.newFixedThreadPool(10);

		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));

			File root = new File("./target/test-files/temp" + appendix + "/");

			deleteRoot(root);

			final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(
					new StringSizeConverter());

			final File dataFolder = new File(root, "data");

			final int expiringValue = 2000;

			final Cache<String, String> cache = CacheFactory.getExpiringFileCache(expiringValue,
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

			Thread.sleep(500);

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

			Thread.sleep(5000);

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
	public void testFilePersistingExpiringCacheTest() {

		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));

			File root = new File("./target/test-files/temp" + appendix + "/");

			deleteRoot(root);

			final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(
					new StringSizeConverter());

			final File dataFolder = new File(root, "data");

			final int expiringValue = 2000;

			final Cache<String, String> cache = CacheFactory.getExpiringFileCache(expiringValue,
					dataFolder, converter);

			final String key = "dfslkjasdfkljsadfa";
			final String value = "dfsaoiuwekljfsdfsadlkaioklalkdsf";

			// TEST PUT, GET, REMOVE, and EXISTS
			for (int x = 0; x < 10; x++) {

				final String keyRepeated = StringUtilities.repeat(key, x);
				final String valueRepeated = StringUtilities.repeat(value, x);

				cache.put(keyRepeated, valueRepeated);

				Thread.sleep(5);

			}

			for (int x = 0; x < 100; x++) {

				final String keyRepeated = StringUtilities.repeat(key, x);

				cache.put(keyRepeated, value);

				if ((x % 100) == 0) {
					logger.debug("Size of data " + dataFolder.getTotalSpace());
				}

			}

			Thread.sleep(expiringValue);

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

			final long timeout = 1000;

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));

			File root = new File("./target/test-files/temp" + appendix + "/");

			deleteRoot(root);

			final Converter<Integer, String> converter = new ReverseConverter<Integer, String>(
					new StringSizeConverter());

			final File dataFolder = new File(root, "data");

			final int expiringValue = 2000;

			final Cache<String, String> cache = CacheFactory.getExpiringFileCache(expiringValue,
					dataFolder, converter);

			final String key = "dfsa";
			final String value = "dfsadsf";
			final String key2 = "fgdd";
			final String value2 = "dfgsds";
			String returnValue;

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

	void deleteRoot(File root) {
		if (root.exists()) {

			if (root.listFiles() != null) {
				for (File cacheFile : root.listFiles()) {
					cacheFile.delete();
				}
			}
			root.delete();
		}
	}

}
