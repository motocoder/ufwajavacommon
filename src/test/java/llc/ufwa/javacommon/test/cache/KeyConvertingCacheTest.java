package llc.ufwa.javacommon.test.cache;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.LongStringConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.cache.FileCache;
import llc.ufwa.data.resource.cache.KeyConvertingCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.data.resource.loader.ResourceLoader;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class KeyConvertingCacheTest {

	private static ResourceLoader<String, byte[]> resourceLoader;
	private static KeyConvertingCache<Long, String, byte[]> testCacheWithResourceLoader;

	private static File rootDir = new File("./test1/");
	private static FileCache fileCache;
	private static KeyConvertingCache<Long, String, byte[]> testCacheWithFileCache;

	private final static String TEST_VAL1 = "Test Value 1";
	private final static String TEST_VAL2 = "Test Value 2";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		LongStringConverter converter = new LongStringConverter();

		resourceLoader = new DefaultResourceLoader<String, byte[]>() {
			@Override
			public byte[] get(String key) throws ResourceException {

				if(key.equals("1")) {
					String retStr = TEST_VAL1;
					return retStr.getBytes();
				} else if(key.equals("2")) {
					String retStr = TEST_VAL2;
					return retStr.getBytes();
				} else {
					return null;
				}
			}

			@Override
			public boolean exists(String key) throws ResourceException {

				if(key.equals("1")) {
					return true;
				} else if(key.equals("2")) {
					return true;
				} else {
					return false;
				}
			}
		};
		testCacheWithResourceLoader = new KeyConvertingCache<Long, String, byte[]> (
				resourceLoader,
				converter
				);

		fileCache = new FileCache(rootDir, 2000, -1); // 2000 is to keep a rogue test case from eating disk, -1 so no worries about expiration.
		Converter<byte[], InputStream> valueConverter = new ReverseConverter<byte[], InputStream> (new InputStreamConverter());
		testCacheWithFileCache = new KeyConvertingCache<Long, String, byte[]> (
				new ValueConvertingCache <String, byte[], InputStream> (fileCache, valueConverter),
				converter
				);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCacheFlavor() {

		deleteRoot(rootDir);

		// Try a put and make sure a file is created.
		testCacheWithFileCache.put(1L, TEST_VAL1.getBytes());
		File expectedFile = new File(rootDir, "1");
		TestCase.assertTrue(expectedFile.exists());

		// Put another an make sure they both have files created.
		testCacheWithFileCache.put(2L, TEST_VAL2.getBytes());
		File expectedFile2 = new File(rootDir, "1");
		TestCase.assertTrue(expectedFile2.exists());
		
		// Test exists().
		try {
			TestCase.assertTrue(testCacheWithFileCache.exists(1L));
		} catch (ResourceException e) {
			System.out.println(e);
			TestCase.fail();
		}
		// Make sure it's not just nodding its head...
		try {
			TestCase.assertFalse(testCacheWithFileCache.exists(3L));
		} catch (ResourceException e) {
			System.out.println(e);
			TestCase.fail();
		}

		// Now test a positive get().
		try {
			byte[] result = testCacheWithFileCache.get(1L);
			TestCase.assertNotNull(result);
			String resultStr = new String(result);
			TestCase.assertTrue(resultStr.equals(TEST_VAL1));
		} catch (Exception e){
			System.out.println(e);
			TestCase.fail();
		}
		// and a negative get().
		try {
			byte[] result = testCacheWithFileCache.get(3L);
			TestCase.assertNull(result);
		} catch (Exception e){
			System.out.println(e);
			TestCase.fail();
		}

		// Try a getAll() with present and missing items, tests negative get() as well.
		List<Long> keys = new ArrayList<Long>();
		keys.add(-1L);
		keys.add(1L);
		keys.add(2L);
		keys.add(3L);

		try {
			List<byte[]> results = testCacheWithFileCache.getAll(keys);
			TestCase.assertNotNull(results);
			TestCase.assertTrue(results.size() == 4);
			logResults(results);
			TestCase.assertTrue(results.get(0) == null);
			TestCase.assertTrue(new String(results.get(1)).equals(TEST_VAL1));
			TestCase.assertTrue(new String(results.get(2)).equals(TEST_VAL2));
			TestCase.assertTrue(results.get(3) == null);
		} catch (ResourceException e) {
			System.out.println(e);
			TestCase.fail();
		}
		
		// Now test remove()
		testCacheWithFileCache.remove(1L);
		TestCase.assertFalse(expectedFile.exists());
		
		// and clear()
		testCacheWithFileCache.clear();
		TestCase.assertFalse(expectedFile2.exists());
		
	}

	@Test
	public void testResourceLoaderFlavor() {

		// Try a put.
		testCacheWithResourceLoader.put(1L, TEST_VAL1.getBytes());

		// Test exists().
		try {
			TestCase.assertTrue(testCacheWithResourceLoader.exists(1L));
		} catch (ResourceException e) {
			System.out.println(e);
			TestCase.fail();
		}
		// Make sure it's not just nodding its head...
		try {
			TestCase.assertFalse(testCacheWithResourceLoader.exists(3L));
		} catch (ResourceException e) {
			System.out.println(e);
			TestCase.fail();
		}

		// Now test a positive get().
		try {
			byte[] result = testCacheWithResourceLoader.get(1L);
			TestCase.assertNotNull(result);
			String resultStr = new String(result);
			TestCase.assertTrue(resultStr.equals(TEST_VAL1));
		} catch (Exception e){
			System.out.println(e);
			TestCase.fail();
		}
		// and a negative get().
		try {
			byte[] result = testCacheWithResourceLoader.get(3L);
			TestCase.assertNull(result);
		} catch (Exception e){
			System.out.println(e);
			TestCase.fail();
		}

		// Try a getAll() with present and missing items, tests negative get() as well.
		List<Long> keys = new ArrayList<Long>();
		keys.add(-1L);
		keys.add(1L);
		keys.add(2L);
		keys.add(3L);

		try {
			List<byte[]> results = testCacheWithResourceLoader.getAll(keys);
			TestCase.assertNotNull(results);
			TestCase.assertTrue(results.size() == 4);
			logResults(results);
			TestCase.assertTrue(results.get(0) == null);
			TestCase.assertTrue(new String(results.get(1)).equals(TEST_VAL1));
			TestCase.assertTrue(new String(results.get(2)).equals(TEST_VAL2));
			TestCase.assertTrue(results.get(3) == null);
		} catch (ResourceException e) {
			System.out.println(e);
			TestCase.fail();
		}
	}

	void deleteRoot (File root) {
		if (root.exists()) {
			for (File cacheFile: root.listFiles()){
				cacheFile.delete();
			}
			root.delete();
		}
	}

	void logResults(List<byte[]> results) {
		for (byte[] result: results) {
			if (result == null){
				System.out.println("null");
			} else {
				System.out.println(new String(result));
			}
		}
	}
}
