package llc.ufwa.data.resource.linear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import llc.ufwa.data.exception.LinearStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomAccessFileWriter implements LinearStreamWriter {
	
	private static final Logger logger = LoggerFactory.getLogger(RandomAccessFileWriter.class);

	private final RandomAccessFile random;
	private final String ACCESS_MODE = "rws";
	
	public RandomAccessFileWriter(final File file) throws LinearStreamException {
		
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

	private int read(byte[] buff) throws LinearStreamException {
		
		int read = -1;
		
		try {
			read = random.read(buff);
		} 
		catch (IOException e) {
			throw new LinearStreamException(e);
		}
		
		return read;
		
	}
	
	@Override
	public int read(long index, byte[] buff) throws LinearStreamException {
		this.seek(index);
		return read(buff);
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
	public void write(long index, byte[] in) throws LinearStreamException {
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
