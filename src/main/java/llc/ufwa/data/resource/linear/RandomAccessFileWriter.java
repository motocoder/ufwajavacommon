package llc.ufwa.data.resource.linear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import llc.ufwa.data.exception.LinearStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomAccessFileWriter implements LinearStreamWriter {
	
	private final RandomAccessFile random;
	private final String ACCESS_MODE = "rws";
	private final int bufferLength;
	
	public RandomAccessFileWriter(final File file, int bufferLength) throws LinearStreamException {
		
		this.bufferLength = bufferLength;
		
		try {
			random = new RandomAccessFile(file, ACCESS_MODE);
		}
		catch (FileNotFoundException e) {
			throw new LinearStreamException(e);
		}
		
	}
	
	private void seek(long index) throws LinearStreamException {
		try {
			random.seek(index);
		} 
		catch (IOException e) {
			throw new LinearStreamException(e);
		}
	}
	
	@Override
	public long length() throws LinearStreamException {
		try {
			return random.length();
		} 
		catch (IOException e) {
			throw new LinearStreamException(e);
		}
	}

	private byte[] read() throws LinearStreamException {
		
		final byte[] buff = new byte[bufferLength];
		
		try {
			random.read(buff);
		}
		catch (IOException e) {
			throw new LinearStreamException(e);
		}
		
		return buff;
		
	}
	
	@Override
	public byte[] read(int index) throws LinearStreamException {
		this.seek(index);
		return read();
	}
	
	private void write(byte[] in) throws LinearStreamException {
		try {
			random.write(in);
		} 
		catch (IOException e) {
			throw new LinearStreamException(e);
		}
	}

	@Override
	public void write(int index, byte[] in) throws LinearStreamException {
		this.seek(index);
		this.write(in);
	}
	
	public void close() throws LinearStreamException {
		try {
			random.close();
		}
		catch (IOException e) {
			throw new LinearStreamException(e);
		}
	}

}
