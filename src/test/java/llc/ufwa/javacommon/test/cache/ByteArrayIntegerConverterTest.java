package llc.ufwa.javacommon.test.cache;

import junit.framework.TestCase;

import org.junit.Test;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.ByteArrayIntegerConverter;

public class ByteArrayIntegerConverterTest {
	
	@Test
	public void testConverter() {
		
		final ByteArrayIntegerConverter converter = new ByteArrayIntegerConverter();
		
		for(int number = -100000; number < 1000000; number ++) {
			
			
			try {
				
				byte[] bytes = converter.restore(number);
			
			
				int converted = converter.convert(bytes);
				
				TestCase.assertEquals(number, converted);
				
			}
			catch (ResourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}

}
