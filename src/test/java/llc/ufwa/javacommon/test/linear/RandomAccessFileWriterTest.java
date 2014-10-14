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
		
		final File file = new File("./target/test-files/temp-hash/");
		final RandomAccessFileWriter writer = new RandomAccessFileWriter(file);
		
		try {
			
			for (int x = 0; x < 20; x++) {
			
				final String initial = "TEST-STRING";
				final byte[] in = initial.getBytes();
				final int size = in.length;
				
				
				final long index = 50;
				writer.write(index, in);
				
				final byte[] out2 = new byte[size];
				writer.read(index, out2);
				
				final String outStringAtIndex = new String(out2, "UTF8");
				
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
