package llc.ufwa.javacommon.test.linear;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;
import llc.ufwa.data.exception.LinearStreamException;
import llc.ufwa.data.resource.linear.ByteArrayWriter;

import org.junit.Test;


public class ByteArrayWriterTest {
	
	@Test 
	public void byteArrayWriterTest() throws LinearStreamException {

		final int ARRAY_SIZE = 1024;
		final String initial = "TEST-STRING";
		final byte[] in = initial.getBytes();
		final int size = in.length;
		
		final ByteArrayWriter writer = new ByteArrayWriter(ARRAY_SIZE, size);
		
		try {
			
			for (int x = 0; x < 5000; x++) {
				
				writer.write(x, in);
				
				final String outStringAtIndex = new String(writer.read(x), "UTF8");
				
				TestCase.assertEquals(initial, outStringAtIndex);
				
			}
		
		}
		catch (UnsupportedEncodingException e) {
			TestCase.fail();
		}
		catch (LinearStreamException e) {
			TestCase.fail(e.getLocalizedMessage());
		}
		finally {
			
			
			
		}
		
	}

}
