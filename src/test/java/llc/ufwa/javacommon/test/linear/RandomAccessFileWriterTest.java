package llc.ufwa.javacommon.test.linear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;
import llc.ufwa.data.resource.linear.RandomAccessFileWriter;

import org.junit.Test;


public class RandomAccessFileWriterTest {

	@Test 
	public void randomAccessFileWriterTest() throws FileNotFoundException {
		
		final File file = new File("./target/test-files/temp-hash/");
		final RandomAccessFileWriter writer = new RandomAccessFileWriter(file);
		
		try {
			
			for (int x = 0; x < 20; x++) {
			
				writer.seek(0);
			
				final String initial = "TEST-STRING";
				final byte[] in = initial.getBytes();
				final int size = in.length;
				
				writer.write(in);
				
				writer.seek(0);
			
				final byte[] out = new byte[size];
				
				writer.read(out);
				
				final String outString = new String(out, "UTF8");
				
				TestCase.assertEquals(initial, outString);
				
				
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
		finally {
			writer.close();
		}
		
		
		
	}
	
}
