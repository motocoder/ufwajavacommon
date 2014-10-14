package llc.ufwa.javacommon.test.linear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;
import llc.ufwa.data.exception.LinearStreamException;
import llc.ufwa.data.resource.linear.RandomAccessFileWriter;

import org.junit.Test;


public class RandomAccessFileWriterTest {

	@Test 
	public void randomAccessFileWriterTest() throws LinearStreamException {

		final String initial = "TEST-STRING";
		final byte[] in = initial.getBytes();
		final int size = in.length;
		
		final File file = new File("./target/test-files/temp-hash/");
		final RandomAccessFileWriter writer = new RandomAccessFileWriter(file, size);
		
		try {
			
			for (int x = 0; x < 5000; x++) {
				
				final int index = 50;
				writer.write(index, in);
				
				final String outStringAtIndex = new String(writer.read(index), "UTF8");
				
				TestCase.assertEquals(initial, outStringAtIndex);
				
			}
		
		}
		catch (UnsupportedEncodingException e) {
			TestCase.fail();
		}
		catch (LinearStreamException e) {
			TestCase.fail();
		}
		finally {
			
			try {
				writer.close();
			}
			catch (LinearStreamException e) {
				TestCase.fail();
			}
			
		}
		
	}
	
}
