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
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.CacheFactory;
import llc.ufwa.data.resource.cache.FileHashCache;
import llc.ufwa.data.resource.cache.FilePersistedMaxCountCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;
import llc.ufwa.util.StringUtilities;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePersistedMaxCountCacheTest {

	private static final Logger logger = LoggerFactory.getLogger(FilePersistedMaxCountCacheTest.class);

	private static final byte[] TEN_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

	static {
		BasicConfigurator.configure();
	}

	boolean removed = false;

	public void removed() {
		removed = true;
	}

	@Test
	public void testFilePersistingCountCacheTest() {

		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));

			File root = new File("./target/test-files/temp" + appendix + "/");

			deleteRoot(root);

			final File dataFolder = new File(root, "data");

			final int maxCount = 50;

			final Callback<Void, String> call = new Callback<Void, String>() {

				@Override
				public Void call(String value) {

					removed();
					return null;
				}

			};

			final Cache<String, String> cache = CacheFactory.getSerializingMaxCountFileCache(
					maxCount, dataFolder, call);

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

			}

			// test exists
			for (int x = 0; x < 10; x++) {

				final String keyRepeated = StringUtilities.repeat(key, x);
				TestCase.assertEquals(false, cache.exists(keyRepeated));

			}

			TestCase.assertEquals(true, removed);

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
	public void testFilePersistingCountCacheMultithreadedTest() {

		final Executor exec = Executors.newFixedThreadPool(10);

		try {

			Random random = new Random();
			String appendix = String.valueOf(Math.abs(random.nextInt()));

			File root = new File("./target/test-files/temp" + appendix + "/");

			deleteRoot(root);

			final File dataFolder = new File(root, "data");

			final int maxCount = 50;

			final Callback<Void, String> call = new Callback<Void, String>() {

				@Override
				public Void call(String value) {

					removed();
					return null;
				}

			};

			final Cache<String, String> cache = CacheFactory.getSerializingMaxCountFileCache(
					maxCount, dataFolder, call);

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

					}

				});

			}

			Thread.sleep(10000);

			// test exists
			for (int x = 0; x < 10; x++) {

				final String keyRepeated = StringUtilities.repeat(key, x);
				TestCase.assertEquals(false, cache.exists(keyRepeated));

			}

			TestCase.assertEquals(true, removed);

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

			final int maxCount = 50;

			final Callback<Void, String> call = new Callback<Void, String>() {

				@Override
				public Void call(String value) {

					removed();
					return null;
				}

			};

			final Cache<String, String> cache = CacheFactory.getSerializingMaxCountFileCache(
					maxCount, dataFolder, call);

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

	@Test
	public void testPersistedPart() {

		Random random = new Random();
		String appendix = String.valueOf(Math.abs(random.nextInt()));

		File root2 = new File("./target/test-files/temp" + appendix + "/");

		deleteRoot(root2);

		final File dataFolder = new File(root2, "data");
		final File tempFolder = new File(root2, "temp");
		final File persistingFolder = new File(root2, "persisting");

		FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);

		Cache<String, String> fileCache = new ValueConvertingCache<String, String, byte[]>(
				new ValueConvertingCache<String, byte[], InputStream>(diskCache,
						new ReverseConverter<byte[], InputStream>(new InputStreamConverter())),
				new SerializingConverter<String>());

		Cache<String, String> cache = new FilePersistedMaxCountCache<String>(persistingFolder,
				fileCache, 2, new Callback<Void, String>() {

					@Override
					public Void call(String value) {
						return null;
					}
				});

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

			cache = new FilePersistedMaxCountCache<String>(persistingFolder, fileCache, 2,
					new Callback<Void, String>() {

						@Override
						public Void call(String value) {
							return null;
						}
					});

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
