package llc.ufwa.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LogUtilTest {
	
	@Test
	public void testLoc() {
		
		String logStr = LogUtil.loc();
		System.out.println(logStr);

		assertTrue(logStr.equalsIgnoreCase("llc.ufwa.util.LogUtilTest testLoc 12 ")); // The number needs to match the line number of the Loc call above.
		
	}

	@Test
	public void testDump() {
		testFuncA();
	}
	
	private void testFuncA() {
	
		String logStr = LogUtil.dump();
		System.out.println(logStr);
		
		assertTrue(logStr.startsWith("Stack dump:\nllc.ufwa.util.LogUtilTest testFuncA 26\nllc.ufwa.util.LogUtilTest testDump 21")); 
	
	}

}
