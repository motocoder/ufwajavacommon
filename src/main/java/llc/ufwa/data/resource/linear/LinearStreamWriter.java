package llc.ufwa.data.resource.linear;

import llc.ufwa.data.exception.LinearStreamException;


public interface LinearStreamWriter {

	public int read(long index, byte[] buff) throws LinearStreamException;
	public void write(long index, byte[] in) throws LinearStreamException;
	
}
