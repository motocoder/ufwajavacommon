package llc.ufwa.javacommon.test.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.FileCache;
import llc.ufwa.data.resource.cache.KeyEncodingCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;

import org.junit.Test;

public class FileCacheTest {

	private static final byte [] TEN_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

	@Test
	public void testDiskCache() {

		File root = new File("./temp1/");
		deleteRoot(root);

		// TODO: add test case for putting a value that's too big for the cache.  

		try {

			final FileCache diskCache = new FileCache(root, 500000, 50000);
			final Cache<String, String> cache = 
					new ValueConvertingCache<String, String, byte []>(
							new ValueConvertingCache<String, byte [], InputStream>( 
									new KeyEncodingCache<InputStream>(
											diskCache
											),
											new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
									),
									new SerializingConverter<String>()
							);


					{
						cache.put("duplicate", "first one");
						TestCase.assertEquals("first one", cache.get("duplicate"));
						cache.put("duplicate", "second one");
						TestCase.assertEquals("second one", cache.get("duplicate"));

						// Cache is empty, get() and exists() should return null.
						TestCase.assertNull(cache.get("hi"));
						TestCase.assertFalse(cache.exists("hi"));

						// Still empty, test getAll().
						final List<String> keys = new ArrayList<String>();

						keys.add("hi1");
						keys.add("hi2");

						final List<String> results = cache.getAll(keys);

						TestCase.assertEquals(2, results.size());
						TestCase.assertNull(results.get(0));
						TestCase.assertNull(results.get(1));

						// Add one item to the cache and test exists() and get().
						cache.put("hi", "test");

						TestCase.assertTrue(cache.exists("hi"));
						TestCase.assertEquals("test", cache.get("hi"));

						// Add two items and test get all. 
						cache.put("hi1", "test1");
						cache.put("hi2", "test2");

						final List<String> results2 = cache.getAll(keys);

						TestCase.assertEquals(results2.get(0), "test1");
						TestCase.assertEquals(results2.get(1), "test2");


						// TODO: Add two more getAll() tests with two keys, one of which is null, null first then null second.

						cache.clear();

					}

					{
						// Not sure about this one, appears to be a duplicate of the getAll() test above.
						TestCase.assertNull(cache.get("hi"));
						TestCase.assertFalse(cache.exists("hi"));

						final List<String> keys = new ArrayList<String>();

						keys.add("hi1");
						keys.add("hi2");

						final List<String> results = cache.getAll(keys);

						TestCase.assertEquals(2, results.size());

						cache.clear();

					}

					{

						// Test remove()
						cache.put("hi", "test");

						TestCase.assertEquals("test", cache.get("hi"));

						cache.remove("hi");

						TestCase.assertNull(cache.get("hi"));

					}

		}
		catch(ResourceException e) {
			TestCase.fail("failed");
		}
		finally {
			root.delete();
		}

	}

	@Test 
	public void testMemoryCache() {

		try {

			File root = new File("./temp2/");

			deleteRoot(root);

			final Cache<String, String> cache = 
					new ValueConvertingCache<String, String, byte []>(
							new ValueConvertingCache<String, byte [], InputStream>(
									new KeyEncodingCache<InputStream>(
											new FileCache(root, -1, -1)
											),
											new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
									),
									new SerializingConverter<String>()
							);

					{

						String testStr = cache.get("hi");
						TestCase.assertNull(testStr);
						TestCase.assertFalse(cache.exists("hi"));

						final List<String> keys = new ArrayList<String>();

						keys.add("hi1");
						keys.add("hi2");

						final List<String> results = cache.getAll(keys);

						TestCase.assertEquals(2, results.size());
						TestCase.assertNull(results.get(0));
						TestCase.assertNull(results.get(1));

						cache.put("hi", "test");

						TestCase.assertTrue(cache.exists("hi"));
						TestCase.assertEquals("test", cache.get("hi"));

						cache.put("hi1", "test1");
						cache.put("hi2", "test2");

						final List<String> results2 = cache.getAll(keys);

						TestCase.assertEquals(results2.get(0), "test1");
						TestCase.assertEquals(results2.get(1), "test2");

						cache.clear();

					}

					{
						TestCase.assertNull(cache.get("hi"));
						TestCase.assertFalse(cache.exists("hi"));

						final List<String> keys = new ArrayList<String>();

						keys.add("hi1");
						keys.add("hi2");

						final List<String> results = cache.getAll(keys);

						TestCase.assertEquals(2, results.size());

						cache.clear();

					}

					{

						cache.put("hi", "test");

						TestCase.assertEquals("test", cache.get("hi"));

						cache.remove("hi");

						TestCase.assertNull(cache.get("hi"));

					}

		}
		catch(Exception e) {

			e.printStackTrace();
			TestCase.fail("should not get here");

		}

	}

	@Test
	public void testMemoryCacheSizeStuff() {

		File root = new File("./temp3/");

		deleteRoot(root);

		final Cache<String, byte []> cache = 
				new ValueConvertingCache<String, byte [], InputStream>(
						new KeyEncodingCache<InputStream>(
								new FileCache(root, 20, -1)
								),
								new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
						);

		try {

			cache.put("1", TEN_BYTES);

			Thread.sleep(50);

			cache.put("2", TEN_BYTES);

			Thread.sleep(50);

			cache.put("3", TEN_BYTES);

			Thread.sleep(50);

			cache.put("4", TEN_BYTES);

			Thread.sleep(50);

			cache.put("5", TEN_BYTES);

		}
		catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		try {

			TestCase.assertNull(cache.get("1"));
			TestCase.assertNull(cache.get("2"));
			TestCase.assertNull(cache.get("3"));
			TestCase.assertNotNull(cache.get("4"));
			TestCase.assertNotNull(cache.get("5"));

		} 
		catch (ResourceException e) {

			TestCase.fail("cant get here");
			e.printStackTrace();

		}

	}

	@Test
	public void testExpires() {

		File root = new File("./temp4/");

		deleteRoot(root);

		final Cache<String, byte []> cache = 
				new ValueConvertingCache<String, byte [], InputStream>(
						new KeyEncodingCache<InputStream>(
								new FileCache(root, 20, 1000)
								),
								new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
						);

		try {

			cache.put("1", TEN_BYTES);

			Thread.sleep(50);

			cache.put("2", TEN_BYTES);

			Thread.sleep(50);

			cache.put("3", TEN_BYTES);

			Thread.sleep(50);

			cache.put("4", TEN_BYTES);

			Thread.sleep(50);

			cache.put("5", TEN_BYTES);

			Thread.sleep(1200);

		}
		catch (InterruptedException e1) {
			TestCase.fail();
		}

		try {

			TestCase.assertNull(cache.get("1"));
			TestCase.assertNull(cache.get("2"));
			TestCase.assertNull(cache.get("3"));
			TestCase.assertNull(cache.get("4"));
			TestCase.assertNull(cache.get("5"));

		} 
		catch (ResourceException e) {

			TestCase.fail("cant get here");
			e.printStackTrace();

		}
	}

	@Test
	public void testCacheCorruption() {

		File root = new File("./temp4/");

		deleteRoot(root);

		// Create a bogus file that's not a directory but has the same name.
		try {
			final FileOutputStream out = new FileOutputStream(root, false);
			String temp = "ABCD";
			out.write(temp.getBytes());
			out.close();
		} catch (Exception e) {
			TestCase.fail();
		}

		final FileCache diskCache = new FileCache(root, 20, 1000);
		final Cache<String, byte []> cache = 
			new ValueConvertingCache<String, byte [], InputStream>(
				new KeyEncodingCache<InputStream>(
					diskCache
					),
					new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
				);

		try {
			// Should fail because a non-directory file exists.
			cache.put("1", TEN_BYTES);
			TestCase.fail();
		}
		catch (RuntimeException e1) {
			// This exception was expected.
		}

		root.delete();

		// Now put a file and delete it externally.
		cache.put("deleteme", TEN_BYTES);
		try {Thread.sleep(5);} catch (Exception e){System.out.println(e);}
		TestCase.assertEquals(10, diskCache.size());

		File fileToDelete = new File(root, "deleteme");
		fileToDelete.delete();

		try {
			TestCase.assertNull(cache.get("deleteme"));
		} catch (Exception e){
			TestCase.fail();
		}
		TestCase.assertEquals(0, diskCache.size());

	}

	@Test
	public void testCacheCorruptionFreeRunning() {

		File root = new File("./temp5/");

		deleteRoot(root);
		
		root.mkdirs();

		final FileCache diskCache = new FileCache(root, 200, 60000);
		final Cache<String, byte []> cache = 
				new ValueConvertingCache<String, byte [], InputStream>(
						new KeyEncodingCache<InputStream>(
								diskCache
								),
								new ReverseConverter<byte [], InputStream>(new InputStreamConverter())
						);

		// Start the bangers.
		FileBanger fileBanger = new FileBanger(root);
		Thread fileBangerThread = new Thread(fileBanger);
		fileBangerThread.start();

		CacheBanger cacheBanger = new CacheBanger(cache);
		Thread cacheBangerThread = new Thread(cacheBanger);
		cacheBangerThread.start();

		// Let them run for a while.
		try {Thread.sleep(3000);} catch (Exception e){System.out.println(e);}

		// Stop the banging.
		fileBanger.stop();
		cacheBanger.stop();

		// Wait a bit then do one more get from the cache to give it a chance to recover.
		try {
			Thread.sleep(1000);
			cache.get("bogus");
		} catch (Exception e) {
			System.out.println(e);
		}

		// Check the cache dir, the size should be number of files * 10.
		long cacheSize = diskCache.size();

		long actualSize = 0;
		
		try {
			File[] filesLeft = root.listFiles();
			if (filesLeft == null) {
			
			
			} else {

				for (File cacheFile: filesLeft){
					actualSize += 10;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}

//		System.out.println(String.format("Actual: %d, Cache reports: %d, Result: %b", actualSize, cacheSize, actualSize == cacheSize));

		TestCase.assertEquals(cacheSize, actualSize);

	}

	private class FileBanger implements Runnable
	{
		private volatile boolean runTask = false;
		File root;

		FileBanger (File rootDir){
			root = rootDir;
		}

		@Override
        public void run()
		{
			runTask = true;
		
			File[] files = new File[5];
			files[0] = new File(root, "File1");
			files[1] = new File(root, "File2");
			files[2] = new File(root, "File3");
			files[3] = new File(root, "File4");
			files[4] = new File(root, "File5");

			int randomIndex;
			
			while(runTask)
			{
				//Do some file creates and deletes directly with the file system.
				randomIndex = (int)(Math.random()*5);
				writeTestFile (files[randomIndex]);

				try {Thread.sleep(10);} catch (Exception e){}
				
				randomIndex = (int)(Math.random()*5);
				files[randomIndex].delete();
				
				try {Thread.sleep(10);} catch (Exception e){}
			}
		}

		private void writeTestFile(File fileToWrite){
			try {
				FileOutputStream out = new FileOutputStream(fileToWrite);
				out.write(TEN_BYTES);
				out.flush();
				out.close();
			} catch (Exception e) {
				System.out.println(e);
			}
			
		}

		public void stop()
		{
			runTask = false;
		}

	}

	class CacheBanger implements Runnable
	{
		private volatile boolean runTask = false;
		final Cache<String, byte []> cache;

		CacheBanger (Cache<String, byte []> inCache){
			cache = inCache;
		}

		@Override
        public void run()
		{
			String[] keys = new String[5];
			keys[0] = "File1";
			keys[1] = "File2";
			keys[2] = "File3";
			keys[3] = "File4";
			keys[4] = "File5";
			
			runTask = true;
			int randomIndex;
			while(runTask)
			{
				//Do a random put and get with the cache.
				randomIndex = (int)(Math.random()*5);
				cache.put(keys[randomIndex], TEN_BYTES);
				
				try {Thread.sleep(10);} catch (Exception e){}
				
				try {
					randomIndex = (int)(Math.random()*5);
					cache.get(keys[randomIndex]);
				} catch (Exception e) {
					System.out.println(e);
				}
				
				try {Thread.sleep(10);} catch (Exception e){}
			}
		}

		public void stop()
		{
			runTask = false;
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
}
