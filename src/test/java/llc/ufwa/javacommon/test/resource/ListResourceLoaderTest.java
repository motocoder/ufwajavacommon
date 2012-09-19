package llc.ufwa.javacommon.test.resource;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.loader.DefaultResourceLoader;
import llc.ufwa.data.resource.loader.ListResourceLoader;
import llc.ufwa.data.resource.loader.ResourceLoader;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ListResourceLoaderTest {
	
	private static ResourceLoader<String, String> mRoot1;
	private static ResourceLoader<String, String> mRoot2;
	private static ArrayList<ResourceLoader<String, String>> mResourceLoaderList;
	private static ListResourceLoader<String, String> mListResourceLoader;

    static {
        BasicConfigurator.configure();
    }
    
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// This loader will behave as if it's empty.
		mRoot1 = new DefaultResourceLoader<String, String>() {
            @Override
            public String get(String key) throws ResourceException {

                if(key.equals("root 1 key")) {
                    return "root 1 value";
                } else {
                	return null;
                }
                
            }
        };

		mRoot2 = new DefaultResourceLoader<String, String>() {
            @Override
            public String get(String key) throws ResourceException {

                if(key.equals("root 2 key")) {
                    return "root 2 value";
                } else {
                	return null;
                }
                
            }
        };
        
        // Create the list of resource loaders to use in the test cases below.
        mResourceLoaderList = new ArrayList<ResourceLoader<String, String>>();
        mResourceLoaderList.add(mRoot1);
        mResourceLoaderList.add(mRoot2);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		mListResourceLoader = null; // Prevent carry-over from one test case to the next.
	}

	@Test
	public void testListResourceLoader() {
		// Test with null list, should throw an exception.
		try{
			mListResourceLoader = new ListResourceLoader<String, String>(null);
			TestCase.fail(); // 
		} catch (Exception e) {
			// OK, this exception was expected.
		} 
		
		// Test with empty list, should throw an exception.
		ArrayList<ResourceLoader<String,String>> emptyList = new ArrayList<ResourceLoader<String, String>>();
		emptyList.clear();
		try{
			mListResourceLoader = new ListResourceLoader<String, String>(emptyList);
			TestCase.fail(); // 
		} catch (Exception e) {
			// OK, this exception was expected.
		} 
		
		// Test with list containing a null element, should throw an exception.
		emptyList.add(null);
		try{
			mListResourceLoader = new ListResourceLoader<String, String>(emptyList);
			TestCase.fail(); // Getting here means an exception was not thrown. 
		} catch (Exception e) {
			// OK, this exception was expected.
		} 
		
		try {
			mListResourceLoader = new ListResourceLoader<String, String>(mResourceLoaderList);
			TestCase.assertNotNull(mListResourceLoader);
		} catch (Exception e) {
			TestCase.fail();
		}
		
	}

	@Test
	public void testExists() {
		try {
			mListResourceLoader = new ListResourceLoader<String, String>(mResourceLoaderList);
			TestCase.assertNotNull(mListResourceLoader);
		} catch (Exception e) {
			TestCase.fail();
		}
		
		boolean retVal;
		
		// Look for null key.
		try {
			retVal = mListResourceLoader.exists(null);
			TestCase.fail();
		} catch (Exception e) {
			// OK, exception was expected.
		}
		
		// Look for non-existent key.
		try {
			retVal = mListResourceLoader.exists("non existent key");
			TestCase.assertFalse(retVal);
		} catch (Exception e) {
			TestCase.fail();
		}
		
		// Look for existing key in the first loader.
		try {
			retVal = mListResourceLoader.exists("root 1 key");
			TestCase.assertTrue(retVal);
		} catch (Exception e) {
			TestCase.fail();
		}
		
		// Look for existing key in the second loader.
		try {
			retVal = mListResourceLoader.exists("root 2 key");
			TestCase.assertTrue(retVal);
		} catch (Exception e) {
			TestCase.fail();
		}
		
	}

	@Test
	public void testGet() {
		try {
			mListResourceLoader = new ListResourceLoader<String, String>(mResourceLoaderList);
			TestCase.assertNotNull(mListResourceLoader);
		} catch (Exception e) {
			TestCase.fail();
		}
		
		String retVal;
		
		// Look for null key.
		try {
			retVal = mListResourceLoader.get(null);
			TestCase.fail();
		} catch (Exception e) {
			// OK, exception was expected.
		}
		
		// Look for non-existent key.
		try {
			retVal = mListResourceLoader.get("non existent key");
			TestCase.assertNull(retVal);
		} catch (Exception e) {
			TestCase.fail();
		}
		
		// Look for existing key in the first loader.
		try {
			retVal = mListResourceLoader.get("root 1 key");
			TestCase.assertTrue(retVal.equals("root 1 value"));
		} catch (Exception e) {
			TestCase.fail();
		}
		
		// Look for existing key in the second loader.
		try {
			retVal = mListResourceLoader.get("root 2 key");
			TestCase.assertTrue(retVal.equals("root 2 value"));
		} catch (Exception e) {
			TestCase.fail();
		}
		
	}

	@Test
	public void testGetAll() {
		try {
			mListResourceLoader = new ListResourceLoader<String, String>(mResourceLoaderList);
			TestCase.assertNotNull(mListResourceLoader);
		} catch (Exception e) {
			TestCase.fail();
		}
		
		List<String> keys = new ArrayList<String>();
		List<String> expectedResults = new ArrayList<String>();
		List<String> actualResults;
		
		// Look for null key list.
		try {
			expectedResults = mListResourceLoader.getAll(null);
			TestCase.fail();
		} catch (Exception e) {
			// OK, the exception was expected.
		}
		
		// Look for null key IN the list.
		keys.clear();
		keys.add(null);
		try {
			expectedResults = mListResourceLoader.getAll(keys);
			TestCase.fail();
		} catch (Exception e) {
			// OK, the exception was expected.
		}
		
		// Look for non-existent key (neither loader).
		keys.clear();
		expectedResults.clear();
		keys.add("non-existent key");
		expectedResults.add(null);
		try {
			actualResults = mListResourceLoader.getAll(keys);
			TestCase.assertTrue(checkGetAllResults(expectedResults, actualResults));
		} catch (Exception e) {
			TestCase.fail();
		}
		
		// Look for existing key in the first loader.
		keys.clear();
		expectedResults.clear();
		keys.add("root 1 key");
		expectedResults.add("root 1 value");
		try {
			actualResults = mListResourceLoader.getAll(keys);
			TestCase.assertTrue(checkGetAllResults(expectedResults, actualResults));
		} catch (Exception e) {
			TestCase.fail();
		}
		
		// Look for existing key in the second loader.
		keys.clear();
		expectedResults.clear();
		keys.add("root 2 key");
		expectedResults.add("root 2 value");
		try {
			actualResults = mListResourceLoader.getAll(keys);
			TestCase.assertTrue(checkGetAllResults(expectedResults, actualResults));
		} catch (Exception e) {
			TestCase.fail();
		}
		
		// Look for existing key in both loaders.
		keys.clear();
		expectedResults.clear();
		keys.add("root 1 key");
		expectedResults.add("root 1 value");
		keys.add("root 2 key");
		expectedResults.add("root 2 value");
		try {
			actualResults = mListResourceLoader.getAll(keys);
			TestCase.assertTrue(checkGetAllResults(expectedResults, actualResults));
		} catch (Exception e) {
			TestCase.fail();
		}
		
	}
	
	private boolean checkGetAllResults (List<String> expectedResults, List<String> actualResults) {
		// Compare the size first.
		if (expectedResults.size() != actualResults.size()) {
			return false;
		}
		
		// Now delete each actual result as it is found.  I chose this method
		// to account for duplicated result values.
		for (int ii = 0; ii < expectedResults.size(); ii++) {
			String expectedValue = expectedResults.get(ii);
			String actualValue = actualResults.get(ii);
			// Check for null cases.
			if (expectedValue == null) {
				if (actualValue != null) {
					return false;
				}
			} else if (!expectedValue.equals(actualValue)) {
				return false;
			}
		}
		return true;
	}

}
