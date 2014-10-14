package llc.ufwa.data.resource.linear;

import llc.ufwa.data.exception.LinearStreamException;


public interface LinearStreamWriter {

	public long length() throws LinearStreamException;
	public byte[] read(int index) throws LinearStreamException;
	public void write(int index, byte[] in) throws LinearStreamException;
	
}
