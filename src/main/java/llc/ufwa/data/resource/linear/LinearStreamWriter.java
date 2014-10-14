package llc.ufwa.data.resource.linear;


public interface LinearStreamWriter {

	public void seek(long index);
	public int read(byte[] buff);
	public int read(long index, byte[] buff);
	public void write(byte[] in);
	public void write(long index, byte[] in);
	
}
